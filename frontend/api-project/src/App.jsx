import { useState } from 'react'
import './App.css'

function App() {
  const [message, setMessage] = useState('')
  const [response, setResponse] = useState('')

  const fetchHello = async () => {
    try {
      const res = await fetch('/api/hello')
      const data = await res.json()
      setMessage(data.message)
    } catch (error) {
      setMessage('연결 실패: ' + error.message)
    }
  }

  const sendData = async () => {
    try {
      const res = await fetch('/api/data', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ test: 'React에서 보낸 데이터' }),
      })
      const data = await res.json()
      setResponse(JSON.stringify(data, null, 2))
    } catch (error) {
      setResponse('전송 실패: ' + error.message)
    }
  }

  return (
    <div style={{ padding: '20px' }}>
      <h1>React + Spring Boot 연결 테스트</h1>
      
      <div style={{ marginBottom: '20px' }}>
        <button onClick={fetchHello}>백엔드에서 메시지 가져오기</button>
        <p>응답: {message}</p>
      </div>
      
      <div>
        <button onClick={sendData}>백엔드로 데이터 전송</button>
        <pre>{response}</pre>
      </div>
    </div>
  )
}

export default App