import { useState, useEffect } from "react";
import axios from "axios";

function useMetrics() {
  const [metrics, setMetrics] = useState([]);

  useEffect(() => {
    let isMounted = true;

    const fetchMetrics = async () => {
        try {
          const response = await axios.get("/api/metrics");
          if (isMounted) {
            // Convert object to array
            const metricsArray = Object.values(response.data || {});
            setMetrics(metricsArray);
          }
        } catch (error) {
          if (isMounted) setMetrics([]);
        }
      };

    fetchMetrics();
    const intervalId = setInterval(fetchMetrics, 3000);

    return () => {
      isMounted = false;
      clearInterval(intervalId);
    };
  }, []);

  return metrics;
}

export default useMetrics;