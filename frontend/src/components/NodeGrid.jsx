import React from "react";

const getBorderColor = (cpu, latency) => {
  if (cpu < 70 && latency < 100) return "#3ec97a";
  else if (cpu < 85 || latency < 200) return "#ffd54f";
  else return "#e57373";
};

const gridStyle = {
  display: "grid",
  gridTemplateColumns: "repeat(3, 1fr)",
  gap: "20px",
};

const cardStyleBase = {
  display: "flex",
  flexDirection: "column",
  borderRadius: "8px",
  boxShadow: "0 2px 8px rgba(0,0,0,0.2)",
  background: "#1a1a2e",
  padding: "18px 18px 15px 15px",
  borderLeft: "8px solid",
  transition: "border-color 0.2s",
  minHeight: "120px",
};

const titleStyle = {
  fontWeight: 600,
  fontSize: "1.1rem",
  marginBottom: "10px",
  color: "#fff",
};

const labelStyle = {
  fontSize: "0.97rem",
  color: "#aaa",
  marginBottom: "4px",
};

const valueStyle = {
  fontWeight: 600,
  color: "#fff",
};

function NodeGrid({ metrics }) {
  return (
    <div style={gridStyle}>
      {metrics && metrics.length > 0 ? (
        metrics.map((node) => {
          const borderColor = getBorderColor(
            Number(node.cpuPercent),
            Number(node.networkLatencyMs)
          );
          return (
            <div
              key={node.nodeId}
              style={{ ...cardStyleBase, borderLeftColor: borderColor }}
            >
              <div style={titleStyle}>{node.nodeId}</div>
              <div style={labelStyle}>
                CPU: <span style={valueStyle}>{node.cpuPercent?.toFixed(1)}%</span>
              </div>
              <div style={labelStyle}>
                Memory: <span style={valueStyle}>{node.memoryPercent?.toFixed(1)}%</span>
              </div>
              <div style={labelStyle}>
                Latency: <span style={valueStyle}>{node.networkLatencyMs?.toFixed(1)} ms</span>
              </div>
            </div>
          );
        })
      ) : (
        <div style={{ gridColumn: "1/-1", textAlign: "center", color: "#888" }}>
          No node metrics available.
        </div>
      )}
    </div>
  );
}

export default NodeGrid;