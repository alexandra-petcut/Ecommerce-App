function Navbar({ currentPage, onNavigate, user, cartCount, onLogout }) {
  return (
    <nav className="navbar">
      <span
        className="nav-brand"
        role="button"
        tabIndex={0}
        onClick={() => onNavigate('products')}
        onKeyDown={(e) => e.key === 'Enter' && onNavigate('products')}
      >
        ShopEase
      </span>

      <div className="nav-links">
        <button
          className={`nav-link ${currentPage === 'products' ? 'active' : ''}`}
          onClick={() => onNavigate('products')}
        >
          Products
        </button>
        <button
          className={`nav-link ${currentPage === 'cart' ? 'active' : ''}`}
          onClick={() => onNavigate('cart')}
        >
          Cart
          {cartCount > 0 && <span className="cart-badge">{cartCount}</span>}
        </button>
        <button
          className={`nav-link ${currentPage === 'orders' ? 'active' : ''}`}
          onClick={() => onNavigate('orders')}
        >
          Orders
        </button>
      </div>

      <div className="nav-auth">
        {user ? (
          <>
            <span className="nav-user">
              Hi, {user.name}
              {user.role === 'ADMIN' && <span className="admin-badge">Admin</span>}
            </span>
            <button className="btn-outline" onClick={onLogout}>
              Log out
            </button>
          </>
        ) : (
          <button
            className={`btn-primary ${currentPage === 'auth' ? 'btn-active' : ''}`}
            onClick={() => onNavigate('auth')}
          >
            Log in
          </button>
        )}
      </div>
    </nav>
  )
}

export default Navbar
