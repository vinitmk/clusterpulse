// import { useState, useEffect } from "react";
// import axios from "axios";

// function useMetrics() {
//   const [metrics, setMetrics] = useState([]);

//   useEffect(() => {
//     let isMounted = true;

//     const fetchMetrics = async () => {
//         try {
//           const response = await axios.get("/api/metrics");
//           if (isMounted) {
//             // Convert object to array
//             const metricsArray = Object.values(response.data || {});
//             setMetrics(metricsArray);
//           }
//         } catch (error) {
//           if (isMounted) setMetrics([]);
//         }
//       };

//     fetchMetrics();
//     const intervalId = setInterval(fetchMetrics, 3000);

//     return () => {
//       isMounted = false;
//       clearInterval(intervalId);
//     };
//   }, []);

//   return metrics;
// }

// export default useMetrics;


import { useEffect, useState } from 'react';
import { Client } from '@stomp/stompjs';

const useMetrics = () => {
  const [metrics, setMetrics] = useState([]);
  const [connected, setConnected] = useState(false);

  useEffect(() => {
    const client = new Client({
      brokerURL: 'ws://localhost:8080/ws/websocket',
      onConnect: () => {
        setConnected(true);
        client.subscribe('/topic/metrics', (message) => {
          const data = JSON.parse(message.body);
          setMetrics(Object.values(data));
        });
      },
      onDisconnect: () => setConnected(false),
      reconnectDelay: 3000,
    });

    client.activate();

    return () => client.deactivate();
  }, []);

  return { metrics, connected };
};

export default useMetrics;