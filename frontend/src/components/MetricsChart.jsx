import React, { useEffect, useState } from "react";
import {
  LineChart,
  Line,
  CartesianGrid,
  XAxis,
  YAxis,
  Tooltip,
  Legend,
  ResponsiveContainer,
} from "recharts";
import axios from "axios";

function MetricsChart({ nodeId }) {
  const [data, setData] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let isMounted = true;

    const fetchHistory = async () => {
      try {
        const response = await axios.get(`/api/metrics/${nodeId}/history`);
        if (isMounted) {
          const raw = response.data || [];
          const chartData = raw.map((entry) => ({
            ...entry,
            timeLabel: entry.timestamp
              ? new Date(entry.timestamp).toLocaleTimeString([], {
                  hour: "2-digit",
                  minute: "2-digit",
                  second: "2-digit",
                })
              : "",
          }));
          setData(chartData);
        }
      } catch (error) {
        if (isMounted) setData([]);
      } finally {
        if (isMounted) setLoading(false);
      }
    };

    fetchHistory();
    const intervalId = setInterval(fetchHistory, 5000);

    return () => {
      isMounted = false;
      clearInterval(intervalId);
    };
  }, [nodeId]);

  if (loading) {
    return (
      <div style={{ textAlign: "center", padding: "20px", color: "#888" }}>
        Loading chart...
      </div>
    );
  }

  return (
    <div style={{ width: "100%", height: 320 }}>
      <ResponsiveContainer>
        <LineChart
          data={data}
          margin={{ top: 30, right: 30, left: 10, bottom: 10 }}
        >
          <CartesianGrid stroke="#2c3862" strokeDasharray="5 5" />
          <XAxis dataKey="timeLabel" tick={{ fontSize: 11, fill: "#aaa" }} />
          <YAxis tick={{ fill: "#aaa" }} />
          <Tooltip
            contentStyle={{ background: "#16213e", border: "none", color: "#fff" }}
          />
          <Legend />
          <Line
            type="monotone"
            dataKey="cpuPercent"
            stroke="#4286f4"
            strokeWidth={2}
            dot={false}
            name="CPU %"
          />
          <Line
            type="monotone"
            dataKey="networkLatencyMs"
            stroke="#ffa726"
            strokeWidth={2}
            dot={false}
            name="Latency (ms)"
          />
        </LineChart>
      </ResponsiveContainer>
    </div>
  );
}

export default MetricsChart;