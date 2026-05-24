/* eslint-disable react-refresh/only-export-components */
import { useEffect, useState } from 'react'

export function Link({ href, children, ...props }) {
  const navigate = (event) => {
    if (href?.startsWith('#') || href?.startsWith('http')) return
    event.preventDefault()
    window.history.pushState({}, '', href)
    window.dispatchEvent(new PopStateEvent('popstate'))
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }

  return <a href={href} onClick={navigate} {...props}>{children}</a>
}

export function useRoute() {
  const [route, setRoute] = useState(window.location.pathname)

  useEffect(() => {
    const updateRoute = () => setRoute(window.location.pathname)
    window.addEventListener('popstate', updateRoute)
    return () => window.removeEventListener('popstate', updateRoute)
  }, [])

  return route
}
