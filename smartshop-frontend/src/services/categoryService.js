const API_URL = '/api/categories'

// Get all categories from Spring Boot
export async function getCategories() {
  const response = await fetch(API_URL)

  if (!response.ok) {
    throw new Error(`Failed to load categories: HTTP ${response.status}`)
  }

  const data = await response.json()

  // Support either a plain array or a paginated response
  if (Array.isArray(data)) {
    return data
  }

  if (Array.isArray(data.content)) {
    return data.content
  }

  throw new Error('Unexpected category API response format')
}
