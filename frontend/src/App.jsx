import React, { useState } from "react";
import NodeGrid from "./components/NodeGrid";
import MetricsChart from "./components/MetricsChart";
import AiNarrator from "./components/AiNarrator";
import useMetrics from "./hooks/useMetrics";

const appBg = {
  minHeight: "100vh",
  minWidth: "100vw",
  background: "#1a1a2e",
  fontFamily: "Inter, Segoe UI, Arial, sans-serif",
};

const headerStyle = {
  width: "100%",
  padding: "26px 0 14px 0",
  paddingLeft: 40,
  boxSizing: "border-box",
  background: "#1a1a2e",
  color: "#fff",
  fontWeight: 800,
  fontSize: "2rem",
  letterSpacing: 1,
  borderBottom: "2.5px solid #232659",
  marginBottom: 0,
};

const sectionStyle = {
  width: "100%",
  maxWidth: 1080,
  margin: "0 auto",
  marginBottom: 34,
  background: "#16213e",
  borderRadius: 12,
  boxShadow: "0 2px 14px rgba(30,30,52,0.19)",
  padding: "32px 40px 28px 36px",
  boxSizing: "border-box",
  color: "#fff"
};

const metricsDropdownStyle = {
  marginBottom: 22,
  display: "flex",
  alignItems: "center"
};

const dropdownLabelStyle = {
  fontWeight: 600,
  fontSize: "1.08rem",
  marginRight: 10,
  color: "#f3f3f7"
};

const selectStyle = {
  padding: "5px 10px",
  borderRadius: 5,
  border: "1px solid #2c3862",
  fontSize: "1rem",
  background: "#23264a",
  color: "#f8f8fa"
};

function App() {
  const metrics = useMetrics();
  const nodeList = Array.isArray(metrics) ? metrics.map(node => node.nodeId) : [];
  const defaultNode = nodeList.length > 0 ? nodeList[0] : "node-1";
  const [selectedNode, setSelectedNode] = useState(defaultNode);

  // Update selected node if list changes & no longer contains the node
  React.useEffect(() => {
    if (!nodeList.includes(selectedNode) && nodeList.length > 0) {
      setSelectedNode(nodeList[0]);
    }
    // eslint-disable-next-line
  }, [metrics]);

  return (
    <div style={appBg}>
      {/* Header */}
      <div style={headerStyle}>
        ClusterPulse
      </div>

      {/* NodeGrid Section */}
      <div style={{ ...sectionStyle, marginTop: 38 }}>
        <h2 style={{
          color: "#fff", fontWeight: 600, marginBottom: 18, letterSpacing: 0.2, fontSize: "1.2rem", marginTop: 0
        }}>
          Nodes Overview
        </h2>
        <NodeGrid metrics={Array.isArray(metrics) ? metrics : []} />
      </div>

      {/* Metrics Chart Section */}
      <div style={{ ...sectionStyle, marginTop: 0 }}>
        <div style={metricsDropdownStyle}>
          <label style={dropdownLabelStyle} htmlFor="node-select">
            Node:
          </label>
          <select
            id="node-select"
            style={selectStyle}
            value={selectedNode}
            onChange={e => setSelectedNode(e.target.value)}
            disabled={nodeList.length === 0}
          >
            {nodeList.length > 0 ? (
              nodeList.map(nid => (
                <option key={nid} value={nid}>
                  {nid}
                </option>
              ))
            ) : (
              <option value="">No Nodes</option>
            )}
          </select>
        </div>
        <MetricsChart metrics={Array.isArray(metrics) ? metrics : []} nodeId={selectedNode} />
      </div>

      {/* AI Narrator Section */}
      <div style={{ ...sectionStyle, marginBottom: 54 }}>
        <h2 style={{
          color: "#fff", fontWeight: 600, marginBottom: 15, letterSpacing: 0.2, fontSize: "1.16rem", marginTop: 0
        }}>
          AI Insights
        </h2>
        <AiNarrator metrics={metrics} />
      </div>
    </div>
  );
}

export default App;