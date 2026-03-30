function OrdersPanel({ orders, onBrowse }) {
  // Format price with comma separators
  function formatPrice(price) {
    return price.toLocaleString('en-US', {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2,
    })
  }

  // Format date string into a readable format (e.g. "Mar 30, 2026")
  function formatDate(dateString) {
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
    })
  }

  return (
    <div className="orders-page">
      <div className="page-header">
        <h1>Order History</h1>
        {orders.length > 0 && <p>Your past purchases</p>}
      </div>

      {/* Empty state — encourage user to start shopping */}
      {orders.length === 0 ? (
        <div className="empty-state">
          <h3>No orders yet</h3>
          <p>Once you place an order, it will show up here.</p>
          <button className="btn-primary" onClick={onBrowse}>
            Start shopping
          </button>
        </div>
      ) : (
        <div className="stack">
          {orders.map((order) => (
            <article key={order.id} className="order-card">
              <div className="order-header">
                <span className="order-id">Order #{order.id}</span>
                <span className="order-status">{order.status}</span>
              </div>
              <div className="order-details">
                <span>Total: ${formatPrice(order.totalAmount)}</span>
                <span>{formatDate(order.createdAt)}</span>
              </div>
            </article>
          ))}
        </div>
      )}
    </div>
  )
}

export default OrdersPanel
