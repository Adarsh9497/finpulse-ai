document.addEventListener('DOMContentLoaded', () => {
    
    // --- Elements ---
    
    // Ingestion Form
    const ingestForm = document.getElementById('ingest-form');
    const ingestBtn = document.getElementById('ingest-btn');
    const ingestBtnText = ingestBtn.querySelector('.btn-text');
    const ingestLoader = ingestBtn.querySelector('.loader');
    const ingestResult = document.getElementById('ingest-result');
    
    // Chat Form
    const chatForm = document.getElementById('chat-form');
    const chatInput = document.getElementById('chat-input');
    const chatHistory = document.getElementById('chat-history');
    const chatBtn = document.getElementById('chat-btn');
    
    // --- Handlers ---
    
    // Handle Ingestion
    ingestForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        
        // Hide previous result
        ingestResult.className = 'result-message hidden';
        
        // Gather data
        const formData = new FormData(ingestForm);
        
        // Set loading state
        setIngestLoading(true);
        
        try {
            const response = await fetch('/api/v1/ingest', {
                method: 'POST',
                // Note: Do NOT set Content-Type header when sending FormData,
                // the browser automatically sets it with the correct boundary.
                body: formData
            });
            
            if (!response.ok) {
                throw new Error(`Server responded with status ${response.status}`);
            }
            
            const data = await response.json();
            
            // Show success
            ingestResult.textContent = data.message;
            ingestResult.className = 'result-message success';
            
            // Clear file input
            document.getElementById('file-upload').value = '';
            
        } catch (error) {
            console.error('Ingestion error:', error);
            ingestResult.textContent = 'Failed to upload document. Check console for details.';
            ingestResult.className = 'result-message error';
        } finally {
            setIngestLoading(false);
        }
    });
    
    // Handle Chat
    chatForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        
        const message = chatInput.value.trim();
        if (!message) return;
        
        // Append user message
        appendMessage('user', message);
        chatInput.value = '';
        
        // Show temporary typing indicator
        const typingId = appendTypingIndicator();
        chatBtn.disabled = true;
        
        try {
            const response = await fetch('/api/v1/chat', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ message })
            });
            
            if (!response.ok) {
                throw new Error(`Server responded with status ${response.status}`);
            }
            
            const data = await response.json();
            
            // Remove typing indicator and append AI response
            removeMessage(typingId);
            appendMessage('system', data.answer || "Sorry, I couldn't generate a response.");
            
        } catch (error) {
            console.error('Chat error:', error);
            removeMessage(typingId);
            appendMessage('system', 'Sorry, an error occurred while processing your request.');
        } finally {
            chatBtn.disabled = false;
        }
    });
    
    // Enter key to submit chat (Shift+Enter for new line)
    chatInput.addEventListener('keydown', (e) => {
        if (e.key === 'Enter' && !e.shiftKey) {
            e.preventDefault();
            chatForm.dispatchEvent(new Event('submit'));
        }
    });
    
    // --- Utility Functions ---
    
    function setIngestLoading(isLoading) {
        if (isLoading) {
            ingestBtn.disabled = true;
            ingestBtnText.classList.add('hidden');
            ingestLoader.classList.remove('hidden');
        } else {
            ingestBtn.disabled = false;
            ingestBtnText.classList.remove('hidden');
            ingestLoader.classList.add('hidden');
        }
    }
    
    function appendMessage(role, text) {
        const msgDiv = document.createElement('div');
        msgDiv.className = `message ${role}-message`;
        
        const avatarDiv = document.createElement('div');
        avatarDiv.className = 'message-avatar';
        avatarDiv.textContent = role === 'user' ? 'YOU' : 'AI';
        
        const contentDiv = document.createElement('div');
        contentDiv.className = 'message-content';
        contentDiv.textContent = text;
        
        msgDiv.appendChild(avatarDiv);
        msgDiv.appendChild(contentDiv);
        
        chatHistory.appendChild(msgDiv);
        chatHistory.scrollTop = chatHistory.scrollHeight;
    }
    
    function appendTypingIndicator() {
        const id = 'typing-' + Date.now();
        const msgDiv = document.createElement('div');
        msgDiv.className = `message system-message`;
        msgDiv.id = id;
        
        const avatarDiv = document.createElement('div');
        avatarDiv.className = 'message-avatar';
        avatarDiv.textContent = 'AI';
        
        const contentDiv = document.createElement('div');
        contentDiv.className = 'message-content';
        contentDiv.innerHTML = '<span style="opacity: 0.7; font-style: italic;">Thinking...</span>';
        
        msgDiv.appendChild(avatarDiv);
        msgDiv.appendChild(contentDiv);
        
        chatHistory.appendChild(msgDiv);
        chatHistory.scrollTop = chatHistory.scrollHeight;
        
        return id;
    }
    
    function removeMessage(id) {
        const el = document.getElementById(id);
        if (el) el.remove();
    }
});
