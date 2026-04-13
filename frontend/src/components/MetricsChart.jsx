import React from 'react';
import {
  BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer,
  CartesianGrid, Legend
} from 'recharts';

const styles = {
  container: { background: '#1a1f2e', border: '1px solid #2d3748', borderRadius: 12, padding: 20 },
  title: { fontSize: 15, fontWeight: 600, marginBottom: 16, color: '#e2e8f0' },
};

function buildChartData(jobs) {
  return jobs.map(j => ({
    name: j.name.length > 14 ? j.name.slice(0, 12) + '…' : j.name,
    Success: j.successCount,
    Failure: j.failureCount,
    Unstable: (j.totalBuilds - j.successCount - j.failureCount) < 0
      ? 0
      : j.totalBuilds - j.successCount - j.failureCount,
  }));
}

const CustomTooltip = ({ active, payload, label }) => {
  if (!active || !payload?.length) return null;
  return (
    <div style={{ background: '#1a1f2e', border: '1px solid #2d3748', borderRadius: 8, padding: '10px 14px', fontSize: 13 }}>
      <div style={{ fontWeight: 600, marginBottom: 6, color: '#e2e8f0' }}>{label}</div>
      {payload.map(p => (
        <div key={p.name} style={{ color: p.fill, marginBottom: 2 }}>
          {p.name}: <strong>{p.value}</strong>
        </div>
      ))}
    </div>
  );
};

export default function MetricsChart({ jobs }) {
  const data = buildChartData(jobs);
  return (
    <div style={styles.container}>
      <div style={styles.title}>Build Results by Pipeline</div>
      <ResponsiveContainer width="100%" height={220}>
        <BarChart data={data} barSize={14} barGap={4}>
          <CartesianGrid strokeDasharray="3 3" stroke="#2d3748" vertical={false} />
          <XAxis dataKey="name" tick={{ fill: '#718096', fontSize: 11 }} axisLine={false} tickLine={false} />
          <YAxis tick={{ fill: '#718096', fontSize: 11 }} axisLine={false} tickLine={false} allowDecimals={false} />
          <Tooltip content={<CustomTooltip />} cursor={{ fill: '#ffffff08' }} />
          <Legend wrapperStyle={{ fontSize: 12, color: '#718096' }} />
          <Bar dataKey="Success" fill="#48bb78" radius={[4, 4, 0, 0]} />
          <Bar dataKey="Failure" fill="#fc8181" radius={[4, 4, 0, 0]} />
          <Bar dataKey="Unstable" fill="#f6ad55" radius={[4, 4, 0, 0]} />
        </BarChart>
      </ResponsiveContainer>
    </div>
  );
}
