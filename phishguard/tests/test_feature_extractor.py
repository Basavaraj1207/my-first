"""
Unit tests for URL Feature Extractor Module
"""

import sys
import os

# Add ml folder to sys.path
sys.path.insert(0, os.path.abspath(os.path.join(os.path.dirname(__file__), "..", "ml")))

from feature_extractor import extract_features, get_feature_vector

def test_legitimate_url_features():
    url = "https://google.com"
    f = extract_features(url)
    assert f["uses_https"] == 1
    assert f["has_ip_address"] == 0
    assert f["has_at_symbol"] == 0
    assert f["subdomain_count"] == 0
    assert f["brand_spoofed"] == 0

def test_ip_based_phishing_url():
    url = "http://192.168.1.1/login.php"
    f = extract_features(url)
    assert f["has_ip_address"] == 1
    assert f["uses_https"] == 0
    assert "login" in f["found_keywords"]

def test_brand_spoofing_clue():
    url = "http://paypal-verification.fake-security-update.xyz/verify"
    f = extract_features(url)
    assert f["brand_spoofed"] == 1
    assert f["subdomain_count"] >= 1
    assert "verify" in f["found_keywords"]

def test_at_symbol_obfuscation():
    url = "http://legit.com@phishing-target.com/signin"
    f = extract_features(url)
    assert f["has_at_symbol"] == 1
    assert "signin" in f["found_keywords"]

def test_feature_vector_dimension():
    url = "https://example.com/test?param=1"
    vec = get_feature_vector(url)
    assert len(vec) == 18
    assert all(isinstance(v, float) for v in vec)

if __name__ == "__main__":
    test_legitimate_url_features()
    test_ip_based_phishing_url()
    test_brand_spoofing_clue()
    test_at_symbol_obfuscation()
    test_feature_vector_dimension()
    print("All 5 Feature Extractor unit tests passed successfully!")
