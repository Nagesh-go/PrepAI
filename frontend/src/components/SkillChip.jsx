export default function SkillChip({ children, muted = false }) {
  return <span className={`skill-chip ${muted ? "muted" : ""}`}>{children}</span>;
}
