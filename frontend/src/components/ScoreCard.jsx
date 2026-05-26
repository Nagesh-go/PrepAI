import Icon from "./Icon.jsx";

export default function ScoreCard({ label, value, suffix, icon, tone = "primary", detail }) {
  return (
    <section className="metric-card">
      <div className="metric-header">
        <span>{label}</span>
        {icon ? (
          <span className={`metric-icon ${tone}`}>
            <Icon name={icon} />
          </span>
        ) : null}
      </div>
      <div className="metric-value">
        {value}
        {suffix ? <small>{suffix}</small> : null}
      </div>
      {detail ? <p className={`metric-detail ${tone}`}>{detail}</p> : null}
    </section>
  );
}
