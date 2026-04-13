import React from 'react';
import {
  RadialBarChart, RadialBar, PolarAngleAxis, ResponsiveContainer, Legend
} from 'recharts';

const styles = {
  container: { background: '#1a1f2e', border: '1px solid #2d3748', borderRadius: 12, padding: 20 },
  title: { fontSize: 15, fontWeight: 600, marginBottom: 8, color: '#e2e8f0' },
  center: { textAlign: 'center', marginTop: -10 },
  rate: { fontSize: 32, fontWeight: 700 },
  label: { fontSize: 12, color: '#718096' },
};

function rateColor(rate) {
  if (rate >= 80) return '#48bb78';
  if (rate >= 50) return '#f6ad55';
  return '#fc8181';
}

export default function SuccessRateChart({ rate }) {
  const color = rateColor(rate);
  const data = [{ value: rate, fill: color }];

  return (
    <div style={styles.container}>
      <div style={styles.title}>Overall Success Rate</div>
      <ResponsiveContainer width="100%" height={160}>
        <RadialBarChart
          cx="50%" cy="80%"
          innerRadius="60%" outerRadius="100%"
          startAngle={180} endAngle={0}
          data={data}
        >
          <PolarAngleAxis type="number" domain={[0, 100]} tick={false} />
          <RadialBar dataKey="value" cornerRadius={8} background={{ fill: '#2d3748' }} />
        </RadialBarChart>
      </ResponsiveContainer>
      <div style={styles.center}>
        <div style={{ ...styles.rate, color }}>{rate}%</div>
        <div style={styles.label}>pipelines succeeding</div>
      </div>
    </div>
  );
}
