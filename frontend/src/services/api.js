const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080'

async function request(path, options = {}, token = '') {  // creates a request = reusable async function
  const headers = {
    'Content-Type': 'application/json', // sending json data
    ...(options.headers || {}), // if extra headers were passed in, add them too
  }

  if (token) {
    headers.Authorization = `Bearer ${token}` // add Authorization header if token exists
  }

  const response = await fetch(`${API_URL}${path}`, { // send request and wait for response
    ...options,
    headers,
  })

  if (!response.ok) { // if request fails
    let errorMessage = 'Something went wrong'
    try {
      const errorData = await response.json() // read error response as json
      errorMessage = errorData.message || errorData.error || errorMessage
    } catch { // if response is not valid json
      errorMessage = response.statusText || errorMessage
    }
    throw new Error(errorMessage)
  }

  if (response.status === 204) { //success -> return null -> avoid parsing empty json
    return null
  }

  return response.json()
}

export function registerUser(payload) {
  return request('/api/auth/register', {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

export function loginUser(payload) {
  return request('/api/auth/login', {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

export function getProducts() {
  return request('/api/products')
}

export function getCart(userId, token) {
  return request(`/api/cart/${userId}`, {}, token)
}

export function addToCart(payload, token) {
  return request('/api/cart/add', {
    method: 'POST',
    body: JSON.stringify(payload),
  }, token)
}

export function updateCartItem(payload, token) {
  return request('/api/cart/update', {
    method: 'PUT',
    body: JSON.stringify(payload),
  }, token)
}

export function removeCartItem(cartItemId, token) {
  return request(`/api/cart/remove/${cartItemId}`, {
    method: 'DELETE',
  }, token)
}

export function checkout(userId, token) {
  return request(`/api/orders/checkout/${userId}`, {
    method: 'POST',
  }, token)
}

export function getOrders(userId, token) {
  return request(`/api/orders/user/${userId}`, {}, token)
}

export function createProduct(payload, token) {
  return request('/api/products', {
    method: 'POST',
    body: JSON.stringify(payload),
  }, token)
}

export function updateProduct(id, payload, token) {
  return request(`/api/products/${id}`, {
    method: 'PUT',
    body: JSON.stringify(payload),
  }, token)
}

export async function uploadProductImage(productId, file, token) {
  const formData = new FormData()
  formData.append('file', file)

  const response = await fetch(`${API_URL}/api/products/${productId}/image`, {
    method: 'POST',
    headers: { Authorization: `Bearer ${token}` }, // no Content-Type — browser sets it with boundary
    body: formData,
  })

  if (!response.ok) {
    let errorMessage = 'Failed to upload image'
    try {
      const errorData = await response.json()
      errorMessage = errorData.message || errorData.error || errorMessage
    } catch { /* ignore */ }
    throw new Error(errorMessage)
  }
}

export function getProductImageUrl(productId) {
  return `${API_URL}/api/products/${productId}/image`
}
