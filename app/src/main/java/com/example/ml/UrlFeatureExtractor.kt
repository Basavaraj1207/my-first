package com.example.ml

import java.net.URI
import java.util.regex.Pattern

data class UrlFeatures(
    val rawUrl: String,
    val scheme: String,
    val domain: String,
    val path: String,
    val query: String,
    val urlLength: Int,
    val domainLength: Int,
    val pathLength: Int,
    val dotCount: Int,
    val hyphenCount: Int,
    val slashCount: Int,
    val digitCount: Int,
    val specialCharCount: Int,
    val queryParamCount: Int,
    val subdomainCount: Int,
    val hasAtSymbol: Boolean,
    val hasIpAddress: Boolean,
    val usesHttps: Boolean,
    val suspiciousKeywordsFound: List<String>,
    val hasDoubleSlashInPath: Boolean,
    val brandSpoofingClue: String?
)

data class DetectionResult(
    val url: String,
    val verdict: String, // LEGITIMATE, SUSPICIOUS, PHISHING
    val riskScore: Int,   // 0 - 100
    val confidence: Int,  // 0 - 100
    val reasons: List<String>,
    val positiveFactors: List<String>,
    val features: UrlFeatures
)

object UrlFeatureExtractor {

    private val IPV4_PATTERN = Pattern.compile(
        "^((25[0-5]|(2[0-4]|1\\d|[1-9]|)\\d)\\.?\\b){4}$"
    )

    private val SUSPICIOUS_KEYWORDS = listOf(
        "login", "verify", "verification", "update", "banking", "secure",
        "account", "signin", "sign-in", "security", "confirm", "wallet",
        "crypto", "free-bonus", "support", "auth", "credential", "billing",
        "ebayisapi", "webscr", "recover", "unlock", "notification"
    )

    private val WELL_KNOWN_BRANDS = listOf(
        "google", "paypal", "apple", "microsoft", "amazon", "netflix",
        "facebook", "instagram", "chase", "bankofamerica", "wellsfargo",
        "binance", "coinbase"
    )

    fun extract(inputUrl: String): UrlFeatures {
        var normalized = inputUrl.trim()
        if (!normalized.startsWith("http://", ignoreCase = true) &&
            !normalized.startsWith("https://", ignoreCase = true)
        ) {
            normalized = "http://$normalized"
        }

        val uri = try {
            URI(normalized)
        } catch (_: Exception) {
            null
        }

        val scheme = uri?.scheme ?: if (normalized.startsWith("https", ignoreCase = true)) "https" else "http"
        val domain = (uri?.host ?: extractDomainFallback(normalized)).lowercase()
        val path = uri?.path ?: ""
        val query = uri?.query ?: ""

        val urlLength = normalized.length
        val domainLength = domain.length
        val pathLength = path.length

        val dotCount = normalized.count { it == '.' }
        val hyphenCount = normalized.count { it == '-' }
        val slashCount = normalized.count { it == '/' }
        val digitCount = normalized.count { it.isDigit() }
        val specialChars = setOf('@', '?', '=', '&', '%', '_', '~', '#', ';', '!')
        val specialCharCount = normalized.count { it in specialChars }

        val queryParamCount = if (query.isBlank()) 0 else query.split('&').size

        // Subdomain count calculation
        val domainParts = domain.split('.').filter { it.isNotBlank() }
        val subdomainCount = if (domainParts.size > 2) domainParts.size - 2 else 0

        val hasAtSymbol = normalized.contains('@')

        val cleanDomainWithoutPort = domain.split(':')[0]
        val hasIpAddress = IPV4_PATTERN.matcher(cleanDomainWithoutPort).matches() ||
                cleanDomainWithoutPort.matches(Regex("^\\d{1,3}(\\.\\d{1,3}){3}$"))

        val usesHttps = scheme.equals("https", ignoreCase = true)

        val lowerUrl = normalized.lowercase()
        val foundKeywords = SUSPICIOUS_KEYWORDS.filter { keyword ->
            lowerUrl.contains(keyword)
        }

        val hasDoubleSlashInPath = path.contains("//")

        // Detect brand spoofing (brand name appears in subdomains or path, but primary domain is something else)
        var brandSpoofing: String? = null
        for (brand in WELL_KNOWN_BRANDS) {
            if (lowerUrl.contains(brand)) {
                val isLegitBrandDomain = domain.endsWith(".$brand.com") ||
                        domain == "$brand.com" ||
                        domain.endsWith(".$brand.org") ||
                        domain == "$brand.org"
                if (!isLegitBrandDomain) {
                    brandSpoofing = "Brand spoofing clue: '$brand' referenced outside authentic domain"
                    break
                }
            }
        }

        return UrlFeatures(
            rawUrl = inputUrl,
            scheme = scheme,
            domain = domain,
            path = path,
            query = query,
            urlLength = urlLength,
            domainLength = domainLength,
            pathLength = pathLength,
            dotCount = dotCount,
            hyphenCount = hyphenCount,
            slashCount = slashCount,
            digitCount = digitCount,
            specialCharCount = specialCharCount,
            queryParamCount = queryParamCount,
            subdomainCount = subdomainCount,
            hasAtSymbol = hasAtSymbol,
            hasIpAddress = hasIpAddress,
            usesHttps = usesHttps,
            suspiciousKeywordsFound = foundKeywords,
            hasDoubleSlashInPath = hasDoubleSlashInPath,
            brandSpoofingClue = brandSpoofing
        )
    }

    private fun extractDomainFallback(url: String): String {
        val withoutScheme = url.removePrefix("http://").removePrefix("https://")
        return withoutScheme.substringBefore('/').substringBefore('?').substringBefore(':')
    }

    fun analyze(inputUrl: String): DetectionResult {
        val f = extract(inputUrl)
        var riskScore = 5
        val reasons = mutableListOf<String>()
        val positives = mutableListOf<String>()

        // 1. IP address in URL: severe red flag
        if (f.hasIpAddress) {
            riskScore += 45
            reasons.add("Direct IP address used instead of canonical domain name")
        } else {
            positives.add("Valid domain name used (no raw IP address)")
        }

        // 2. @ symbol: severe credential theft pattern
        if (f.hasAtSymbol) {
            riskScore += 35
            reasons.add("Contains '@' symbol: browser credential spoofing technique")
        }

        // 3. Brand spoofing clue
        if (f.brandSpoofingClue != null) {
            riskScore += 30
            reasons.add(f.brandSpoofingClue)
        }

        // 4. Excessive subdomains
        if (f.subdomainCount >= 3) {
            riskScore += 25
            reasons.add("Excessive subdomains (${f.subdomainCount}) often used to obfuscate host")
        } else if (f.subdomainCount >= 2) {
            riskScore += 12
            reasons.add("Multiple subdomains detected (${f.subdomainCount})")
        } else {
            positives.add("Normal domain depth structure")
        }

        // 5. Suspicious keywords
        if (f.suspiciousKeywordsFound.isNotEmpty()) {
            val kwScore = minOf(35, f.suspiciousKeywordsFound.size * 12)
            riskScore += kwScore
            reasons.add("Suspicious auth/security keywords detected: ${f.suspiciousKeywordsFound.joinToString(", ")}")
        } else {
            positives.add("No sensitive credential theft keywords found")
        }

        // 6. Excessive URL length (> 75 chars is suspicious, > 100 is high risk)
        if (f.urlLength > 100) {
            riskScore += 20
            reasons.add("Unusually long URL (${f.urlLength} chars) common in phishing vectors")
        } else if (f.urlLength > 75) {
            riskScore += 10
            reasons.add("Extended URL length (${f.urlLength} characters)")
        } else {
            positives.add("Standard URL length (${f.urlLength} characters)")
        }

        // 7. Excessive dots or hyphens
        if (f.dotCount >= 5) {
            riskScore += 15
            reasons.add("High dot count (${f.dotCount}) indicating nested obfuscation")
        }
        if (f.hyphenCount >= 4) {
            riskScore += 15
            reasons.add("High hyphen count (${f.hyphenCount}) frequently seen in typosquatting")
        }

        // 8. Protocol HTTPS vs HTTP
        if (!f.usesHttps) {
            riskScore += 20
            reasons.add("Insecure HTTP protocol: lacks SSL/TLS transport encryption")
        } else {
            positives.add("Uses HTTPS transport security")
        }

        // 9. Double slash redirect
        if (f.hasDoubleSlashInPath) {
            riskScore += 20
            reasons.add("Double slash '//' detected in path: open redirect pattern")
        }

        // Normalize risk score to 0..100
        riskScore = riskScore.coerceIn(4, 99)

        // Classify Verdict
        val verdict = when {
            riskScore >= 70 -> "PHISHING"
            riskScore >= 38 -> "SUSPICIOUS"
            else -> "LEGITIMATE"
        }

        // Confidence calculation (ML ensemble confidence simulation based on feature clarity)
        val confidence = when {
            riskScore > 85 || riskScore < 15 -> (94..98).random()
            riskScore in 65..85 || riskScore in 15..35 -> (88..94).random()
            else -> (80..88).random()
        }

        return DetectionResult(
            url = inputUrl,
            verdict = verdict,
            riskScore = riskScore,
            confidence = confidence,
            reasons = if (reasons.isEmpty()) listOf("Standard URL lexical patterns detected") else reasons,
            positiveFactors = positives,
            features = f
        )
    }
}
