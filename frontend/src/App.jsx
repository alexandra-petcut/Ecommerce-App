import { useEffect, useState } from 'react'
import Navbar from './components/Navbar'
import AuthPanel from './components/AuthPanel'
import ProductList from './components/ProductList'
import CartPanel from './components/CartPanel'
import OrdersPanel from './components/OrdersPanel'
import {
  getProducts,
  getCart,
  addToCart,
  updateCartItem,
  removeCartItem,
  checkout,
  getOrders,
  createProduct,
  updateProduct,
  uploadProductImage,
} from './services/api'
import { clearSession, getStoredSession, saveSession } from './utils/auth'


function App() {
  const [session, setSession] = useState(getStoredSession())
  const [products, setProducts] = useState([])
  const [cartItems, setCartItems] = useState([])
  const [orders, setOrders] = useState([])
  const [loadingProducts, setLoadingProducts] = useState(true)
  const [currentPage, setCurrentPage] = useState('products') // 'products' | 'cart' | 'orders' | 'auth'
  const [notification, setNotification] = useState(null) // { message, type }

  const user = session?.user ?? null // if session exists, use session.user, else use null
  const isAdmin = user?.role === 'ADMIN'
  const cartCount = cartItems.reduce((sum, item) => sum + item.quantity, 0) // total items in cart

  // Auto-dismiss notification after 4 seconds
  useEffect(() => {
    if (!notification) return
    const timer = setTimeout(() => setNotification(null), 4000)
    return () => clearTimeout(timer) // cleanup if notification changes before timeout
  }, [notification])

  useEffect(() => {
    loadProducts() // load products on first render
  }, [])

  useEffect(() => {
    if (!user?.id) { // if user isnt logged in, set cart and orders empty
      setCartItems([])
      setOrders([])
      return
    }

    loadCart(user.id) // load cart and orders if user is logged in
    loadOrders(user.id)
  }, [user?.id])

  // Show a notification toast
  function showNotification(message, type = 'info') {
    setNotification({ message, type })
  }

  // Navigate to a page (with login check for cart/orders)
  function navigateTo(page) {
    if ((page === 'cart' || page === 'orders') && !user) {
      showNotification('Please log in to access this page.', 'warning')
      setCurrentPage('auth')
      return
    }
    setCurrentPage(page)
  }

  async function loadProducts() { // function to load products; await = wait for result
    try {
      setLoadingProducts(true)
      const data = await getProducts()
      setProducts(data)
    } catch (error) {
      showNotification(error.message, 'error')
    } finally {
      setLoadingProducts(false)
    }
  }

  async function loadCart(userId) { // function to load cart
    try {
      const data = await getCart(userId, session?.token)
      setCartItems(data)
    } catch (error) {
      showNotification(error.message, 'error')
    }
  }

  async function loadOrders(userId) { // function to load orders
    try {
      const data = await getOrders(userId, session?.token)
      setOrders(data)
    } catch (error) {
      showNotification(error.message, 'error')
    }
  }

  function handleAuthSuccess(authData) {
    const nextSession = { // create session object
      user: {
        id: authData.id,
        name: authData.name,
        email: authData.email,
        role: authData.role,
      },
      token: authData.token ?? '', // use authData.token if it exists, else use ''
    }

    saveSession(nextSession)
    setSession(nextSession) // save and set current session
    showNotification(`Welcome, ${authData.name}!`, 'success')
    setCurrentPage('products') // go to products after login
  }

  function handleLogout() { // function for logout
    clearSession()
    setSession(null)
    setCurrentPage('products')
    showNotification('You have been logged out.', 'info')
  }

  async function handleCreateProduct(productData, imageFile) {
    try {
      const created = await createProduct(productData, session?.token)
      if (imageFile && created?.id) {
        await uploadProductImage(created.id, imageFile, session?.token)
      }
      await loadProducts()
      showNotification('Product created successfully!', 'success')
    } catch (error) {
      showNotification(error.message, 'error')
    }
  }

  async function handleUpdateProduct(id, productData, imageFile) {
    try {
      await updateProduct(id, productData, session?.token)
      if (imageFile) {
        await uploadProductImage(id, imageFile, session?.token)
      }
      await loadProducts()
      showNotification('Product updated successfully!', 'success')
    } catch (error) {
      showNotification(error.message, 'error')
    }
  }

  async function handleAddToCart(productId) { // add product to cart
    if (!user?.id) {
      showNotification('Please log in before adding products to the cart.', 'warning')
      setCurrentPage('auth')
      return
    }

    try {
      await addToCart(
        { userId: user.id, productId, quantity: 1 }, // request body
        session?.token, // auth token
      )
      await loadCart(user.id) // refresh cart
      showNotification('Product added to cart.', 'success')
    } catch (error) {
      showNotification(error.message, 'error')
    }
  }

  async function handleQuantityChange(cartItemId, quantity) { // change item quantity
    if (!user?.id) return

    try {
      await updateCartItem({ cartItemId, quantity }, session?.token) // update item then refresh cart
      await loadCart(user.id)
    } catch (error) {
      showNotification(error.message, 'error')
    }
  }

  async function handleRemove(cartItemId) { // delete item from cart
    if (!user?.id) return

    try {
      await removeCartItem(cartItemId, session?.token)
      await loadCart(user.id)
      showNotification('Item removed from cart.', 'info')
    } catch (error) {
      showNotification(error.message, 'error')
    }
  }

  async function handleCheckout() { // place order from cart
    if (!user?.id) return

    try {
      await checkout(user.id, session?.token)
      await loadCart(user.id)
      await loadOrders(user.id)
      showNotification('Order placed successfully!', 'success')
      setCurrentPage('orders') // show the new order
    } catch (error) {
      showNotification(error.message, 'error')
    }
  }

  return (
    <>
      {/* Navigation bar — always visible */}
      <Navbar
        currentPage={currentPage}
        onNavigate={navigateTo}
        user={user}
        cartCount={cartCount}
        onLogout={handleLogout}
      />

      {/* Notification toast — auto-dismisses */}
      {notification && (
        <div className={`notification ${notification.type}`}>
          {notification.message}
        </div>
      )}

      {/* Page content — only one page visible at a time */}
      <div className="page-container">
        {currentPage === 'auth' && (
          <AuthPanel onAuthSuccess={handleAuthSuccess} />
        )}

        {currentPage === 'products' && (
          <ProductList
            products={products}
            loading={loadingProducts}
            onAddToCart={handleAddToCart}
            isLoggedIn={!!user}
            isAdmin={isAdmin}
            onCreateProduct={handleCreateProduct}
            onUpdateProduct={handleUpdateProduct}
          />
        )}

        {currentPage === 'cart' && (
          <CartPanel
            cartItems={cartItems}
            onQuantityChange={handleQuantityChange}
            onRemove={handleRemove}
            onCheckout={handleCheckout}
            onBrowse={() => navigateTo('products')}
          />
        )}

        {currentPage === 'orders' && (
          <OrdersPanel
            orders={orders}
            onBrowse={() => navigateTo('products')}
          />
        )}
      </div>
    </>
  )
}

export default App
