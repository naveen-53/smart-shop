
const API_URL = '/api/categories'

// GET: Fetch all categories
export async function getCategories() {
  const response = await fetch(API_URL)

  if (!response.ok) {
    throw new Error(`Failed to load categories: HTTP ${response.status}`)
  }

  const data = await response.json()

  // Support a plain array or a paginated response
  if (Array.isArray(data)) {
    return data
  }

  if (Array.isArray(data.content)) {
    return data.content
  }

  throw new Error('Unexpected category API response format')
}

// POST: Create a category
export async function createCategory(category) {
  const response = await fetch(API_URL, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(category),
  })

  if (!response.ok) {
    const message = await response.text()
    throw new Error(
      message || `Failed to create category: HTTP ${response.status}`
    )
  }

  // Support APIs that return a body or no body
  const text = await response.text()
  return text ? JSON.parse(text) : null
}

// PUT: Update an existing category
export async function updateCategory(id, category) {
  const response = await fetch(`${API_URL}/${id}`, {
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(category),
  })

  if (!response.ok) {
    const message = await response.text()
    throw new Error(
      message || `Failed to update category: HTTP ${response.status}`
    )
  }

  const text = await response.text()
  return text ? JSON.parse(text) : null
}

// PATCH: Activate or deactivate a category
export async function updateCategoryStatus(id, active) {
  const response = await fetch(`${API_URL}/${id}/status`, {
    method: 'PATCH',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({ active }),
  })

  if (!response.ok) {
    const message = await response.text()
    throw new Error(
      message || `Failed to update status: HTTP ${response.status}`
    )
  }

  return true
}
