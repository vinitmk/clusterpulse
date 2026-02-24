import React, { useState, useRef } from "react";

function AiNarrator() {
  const [output, setOutput] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const outputRef = useRef(null);

  const handleClick = async () => {
    setOutput("");
    setError("");
    setLoading(true);
    try {
      const response = await fetch("/api/ai/analyze", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
      });

      if (!response.ok) {
        throw new Error(`Error: ${response.status} ${response.statusText}`);
      }

      if (!response.body || !window.ReadableStream) {
        // Non-streaming fallback:
        const text = await response.text();
        setOutput(text);
        setLoading(false);
        return;
      }

      const reader = response.body.getReader();
      const decoder = new TextDecoder();
      let done = false;

      let buffer = "";

      // Stream loop
      while (!done) {
        const { value, done: streamDone } = await reader.read();
        done = streamDone;
        if (value) {
          buffer += decoder.decode(value, { stream: !done });
          setOutput((prev) => {
            // To avoid repeated state updates causing choppy UI, use buffer
            return buffer;
          });
          // Optional: scroll to bottom as content arrives
          if (outputRef.current) {
            outputRef.current.scrollTop = outputRef.current.scrollHeight;
          }
        }
      }
    } catch (err) {
      setError(err.message || "Failed to analyze cluster.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ maxWidth: 600, margin: "40px auto 0 auto" }}>
      <button
        onClick={handleClick}
        disabled={loading}
        style={{
          background: "#202136",
          color: "#fff",
          padding: "12px 26px",
          borderRadius: 8,
          border: "none",
          cursor: loading ? "not-allowed" : "pointer",
          fontWeight: 600,
          fontSize: "1rem",
        }}
      >
        {loading ? (
          <span style={{ display: "flex", alignItems: "center" }}>
            <span
              className="spinner"
              style={{
                width: 18,
                height: 18,
                marginRight: 9,
                border: "3px solid #fff",
                borderTop: "3px solid #5460bb",
                borderRadius: "50%",
                display: "inline-block",
                animation: "spin 1s linear infinite"
              }}
            />
            Analyzing...
          </span>
        ) : (
          "Analyze Cluster Health"
        )}
      </button>
      <div
        ref={outputRef}
        style={{
          minHeight: 130,
          marginTop: 24,
          background: "#18192a",
          color: "#f8f8fa",
          borderRadius: "9px",
          fontSize: "1.04rem",
          padding: "18px 22px",
          fontFamily: "Consolas, 'Fira Mono', 'Menlo', monospace",
          whiteSpace: "pre-wrap",
          lineHeight: 1.56,
          overflowY: "auto",
          border: "1.5px solid #232659"
        }}
      >
        {output}
        {error && (
          <div
            style={{
              marginTop: 12,
              color: "#ff8585",
              fontWeight: 500,
            }}
          >
            {error}
          </div>
        )}
      </div>
      {/* Spinner keyframes in component */}
      <style>
        {`@keyframes spin {
            0% { transform: rotate(0deg);}
            100% { transform: rotate(360deg);}
          }`}
      </style>
    </div>
  );
}

export default AiNarrator;