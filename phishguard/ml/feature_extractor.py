"""
PhishGuard - Lexical & Domain Feature Extraction Module
Extracts 18 defensive characteristics from URL strings without executing remote code.
"""

import re
import math
from urllib.parse import urlparse
from typing import Dict, Any, List

IPV4_PATTERN = re.compile(r"^((25[0-5]|(2[0-4]|1\d|[1-9]|)\d)\.?\b){4}$")

SUSPICIOUS_KEYWORDS = [
    "login", "verify", "verification", "update", "banking", "secure",
    "account", "signin", "sign-in", "security", "confirm", "wallet",
    "crypto", "free-bonus", "support", "auth", "credential", "billing",
    "ebayisapi", "webscr", "recover", "unlock", "notification", "password"
]

TOP_TARGETED_BRANDS = [
    "google", "paypal", "apple", "microsoft", "amazon", "netflix",
    "facebook", "instagram", "chase", "bankofamerica", "wellsfargo",
    "binance", "coinbase"
]

def calculate_shannon_entropy(text: str) -> float:
    """Calculates the Shannon entropy (randomness) of a string."""
    if not text:
        return 0.0
    entropy = 0.0
    length = len(text)
    char_counts = {}
    for char in text:
        char_counts[char] = char_counts.get(char, 0) + 1
    for count in char_counts.values():
        p = count / length
        entropy -= p * math.log2(p)
    return round(entropy, 4)

def extract_features(raw_url: str) -> Dict[str, Any]:
    """
    Extracts 18 lexical and domain features from a given URL string.
    Returns a dictionary of raw features and normalized numerical values for ML.
    """
    url = raw_url.strip()
    if not url.startswith(("http://", "https://")):
        url = "http://" + url

    parsed = urlparse(url)
    scheme = parsed.scheme.lower()
    netloc = parsed.netloc.lower()
    path = parsed.path
    query = parsed.query

    # Hostname without port
    host = netloc.split(":")[0]

    # Feature 1: URL length
    url_length = len(url)

    # Feature 2: Domain length
    domain_length = len(host)

    # Feature 3: Path length
    path_length = len(path)

    # Feature 4: Dot count
    dot_count = url.count(".")

    # Feature 5: Hyphen count
    hyphen_count = url.count("-")

    # Feature 6: Slash count
    slash_count = url.count("/")

    # Feature 7: Digit count
    digit_count = sum(c.isdigit() for c in url)

    # Feature 8: Special character count (@, ?, =, &, %, _, ~, #)
    special_chars = set("@?=&%_~#;!")
    special_char_count = sum(c in special_chars for c in url)

    # Feature 9: Query parameter count
    query_param_count = len(query.split("&")) if query else 0

    # Feature 10: Subdomain count
    domain_parts = [p for p in host.split(".") if p]
    subdomain_count = max(0, len(domain_parts) - 2)

    # Feature 11: Contains '@' (browser credential trick)
    has_at_symbol = 1 if "@" in url else 0

    # Feature 12: Direct IP address detection
    has_ip_address = 1 if IPV4_PATTERN.match(host) or bool(re.match(r"^\d{1,3}(\.\d{1,3}){3}$", host)) else 0

    # Feature 13: HTTPS usage
    uses_https = 1 if scheme == "https" else 0

    # Feature 14: Suspicious keyword presence & count
    url_lower = url.lower()
    found_keywords = [kw for kw in SUSPICIOUS_KEYWORDS if kw in url_lower]
    suspicious_keyword_count = len(found_keywords)

    # Feature 15: Double slash in path (redirect trick)
    has_double_slash_path = 1 if "//" in path else 0

    # Feature 16: Shannon entropy of URL
    entropy = calculate_shannon_entropy(url)

    # Feature 17: Digit ratio
    digit_ratio = round(digit_count / max(1, url_length), 4)

    # Feature 18: Brand spoofing clue (brand in subdomain or path, but not root domain)
    brand_spoofed = 0
    brand_name = None
    for brand in TOP_TARGETED_BRANDS:
        if brand in url_lower:
            is_official = (
                host.endswith(f".{brand}.com") or host == f"{brand}.com" or
                host.endswith(f".{brand}.org") or host == f"{brand}.org"
            )
            if not is_official:
                brand_spoofed = 1
                brand_name = brand
                break

    return {
        "raw_url": raw_url,
        "url_length": url_length,
        "domain_length": domain_length,
        "path_length": path_length,
        "dot_count": dot_count,
        "hyphen_count": hyphen_count,
        "slash_count": slash_count,
        "digit_count": digit_count,
        "special_char_count": special_char_count,
        "query_param_count": query_param_count,
        "subdomain_count": subdomain_count,
        "has_at_symbol": has_at_symbol,
        "has_ip_address": has_ip_address,
        "uses_https": uses_https,
        "suspicious_keyword_count": suspicious_keyword_count,
        "has_double_slash_path": has_double_slash_path,
        "entropy": entropy,
        "digit_ratio": digit_ratio,
        "brand_spoofed": brand_spoofed,
        # Informational metadata
        "found_keywords": found_keywords,
        "spoofed_brand": brand_name
    }

def get_feature_vector(raw_url: str) -> List[float]:
    """Returns the ordered numerical feature vector required for the ML model."""
    f = extract_features(raw_url)
    return [
        float(f["url_length"]),
        float(f["domain_length"]),
        float(f["path_length"]),
        float(f["dot_count"]),
        float(f["hyphen_count"]),
        float(f["slash_count"]),
        float(f["digit_count"]),
        float(f["special_char_count"]),
        float(f["query_param_count"]),
        float(f["subdomain_count"]),
        float(f["has_at_symbol"]),
        float(f["has_ip_address"]),
        float(f["uses_https"]),
        float(f["suspicious_keyword_count"]),
        float(f["has_double_slash_path"]),
        float(f["entropy"]),
        float(f["digit_ratio"]),
        float(f["brand_spoofed"])
    ]

if __name__ == "__main__":
    test_urls = [
        "https://google.com",
        "http://secure-update-paypal-verify.info/auth",
        "http://192.168.1.105/bank-login.php?user=admin@test.com"
    ]
    print("=" * 60)
    print("PHISHGUARD FEATURE EXTRACTOR VERIFICATION")
    print("=" * 60)
    for u in test_urls:
        res = extract_features(u)
        print(f"\nURL: {u}")
        print(f"  Length: {res['url_length']} | Dots: {res['dot_count']} | HTTPS: {res['uses_https']}")
        print(f"  IP Host: {res['has_ip_address']} | @ Symbol: {res['has_at_symbol']}")
        print(f"  Subdomains: {res['subdomain_count']} | Keywords: {res['found_keywords']}")
        print(f"  Entropy: {res['entropy']} | Brand Spoofed: {res['brand_spoofed']}")
        print(f"  Vector (18 features): {get_feature_vector(u)}")
