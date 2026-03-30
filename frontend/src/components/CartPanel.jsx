function CartPanel({ cartItems, onQuantityChange, onRemove, onCheckout, onBrowse }) {
  // Calculate total price of all items in cart
  const total = cartItems.reduce((sum, item) => {
    const price = item.product?.price || 0
    return sum + price * item.quantity
  }, 0)

  // Format price with comma separators
  function formatPrice(price) {
    return price.toLocaleString('en-US', {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2,
    })
  }

  return (
    <div className="cart-page">
      <div className="page-header">
        <h1>Shopping Cart</h1>
        {cartItems.length > 0 && (
          <p>
            {cartItems.length} item{cartItems.length !== 1 ? 's' : ''} in your
            cart
          </p>
        )}
      </div>

      {/* Empty state — show a friendly message with link to products */}
      {cartItems.length === 0 ? (
        <div className="empty-state">
          <h3>Your cart is empty</h3>
          <p>Looks like you haven't added any items yet.</p>
          <button className="btn-primary" onClick={onBrowse}>
            Browse products
          </button>
        </div>
      ) : (
        <>
          {/* Cart items list */}
          <div className="stack">
            {cartItems.map((item) => (
              <div key={item.id} className="cart-item">
                <div className="cart-item-info">
                  <strong>{item.product?.name || 'Product'}</strong>
                  <p className="item-price">
                    ${formatPrice(item.product?.price || 0)} each
                  </p>
                </div>

                <div className="cart-item-actions">
                  <input
                    className="quantity-input"
                    type="number"
                    min="1"
                    value={item.quantity}
                    aria-label={`Quantity for ${item.product?.name || 'product'}`}
                    onChange={(event) =>
                      onQuantityChange(item.id, Number(event.target.value))
                    }
                  />
                  <button
                    className="btn-danger"
                    onClick={() => onRemove(item.id)}
                  >
                    Remove
                  </button>
                </div>
              </div>
            ))}
          </div>

          {/* Cart total and checkout button */}
          <div className="cart-summary">
            <span className="cart-total">Total: ${formatPrice(total)}</span>
            <button className="btn-success" onClick={onCheckout}>
              Checkout
            </button>
          </div>
        </>
      )}
    </div>
  )
}

export default CartPanel
