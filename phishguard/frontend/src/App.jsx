import React, { useState } from 'react'

export default function App() {
  const [url, setUrl] = useState('')
  const [status, setStatus] = useState('PhishGuard System Initialized (Phase 1 Ready)')

  return (
    <div style={{
      minHeight: '100vh',
      backgroundColor: '#0a0f1d',
      color: '#f1f5f9',
      fontFamily: 'system-ui, -apple-system, sans-serif',
      padding: '40px 20px',
      display: 'flex',
      flexDirection: 'column',
      alignItems: 'center'
    }}>
      <div style={{ maxWidth: '800px', width: '100%' }}>
        <header style={{ textAlign: 'center', marginBottom: '40px' }}>
          <div style={{
            display: 'inline-block',
            padding: '8px 16px',
            backgroundColor: 'rgba(0, 229, 255, 0.1)',
            border: '1px solid rgba(0, 229, 255, 0.3)',
            borderRadius: '9999px',
            color: '#00e5ff',
            fontSize: '12px',
            fontWeight: '600',
            letterSpacing: '1px',
            marginBottom: '16px'
          }}>
            PHISHGUARD AI SECURITY
          </div>
          <h1 style={{ fontSize: '36px', fontWeight: '800', margin: '0 0 12px 0' }}>
            PhishGuard – Phishing Website Detection System
          </h1>
          <p style={{ color: '#94a3b8', fontSize: '16px', margin: 0 }}>
            Detect suspicious websites before you click using lexical feature extraction & machine learning.
          </p>
        </header>

        <main style={{
          backgroundColor: '#111c34',
          borderRadius: '16px',
          border: '1px solid #1a2645',
          padding: '32px',
          boxShadow: '0 20px 25px -5px rgba(0, 0, 0, 0.5)'
        }}>
          <h2 style={{ fontSize: '18px', fontWeight: '700', marginBottom: '8px', color: '#00e5ff' }}>
            Phase 1: Environment & Architecture Verification
          </h2>
          <p style={{ color: '#94a3b8', fontSize: '14px', lineHeight: '1.6' }}>
            The full-stack directory hierarchy, virtual environments, backend schemas, and ML pipeline structure have been verified.
          </p>
          <div style={{
            marginTop: '20px',
            padding: '16px',
            backgroundColor: '#0a0f1d',
            borderRadius: '8px',
            border: '1px solid #26395e',
            fontSize: '13px',
            color: '#00e676'
          }}>
            Status: {status}
          </div>
        </main>
      </div>
    </div>
  )
}
