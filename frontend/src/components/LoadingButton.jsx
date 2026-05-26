export default function LoadingButton({ loading, children, className = "", type = "submit", ...props }) {
  return (
    <button type={type} className={className} disabled={loading || props.disabled} {...props}>
      {loading ? "Please wait..." : children}
    </button>
  );
}
