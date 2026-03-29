import express from 'express';
import cors from 'cors';
import dotenv from 'dotenv';

// Load environment variables
 dotenv.config();

const app = express();
const PORT = process.env.PORT || 3000;

// Middleware
app.use(cors());
app.use(express.json());

// Health check endpoint
app.get('/api/health', (req, res) => {
  res.json({ status: 'ok', timestamp: new Date().toISOString() });
});

// Game state endpoint
app.get('/api/game/state', (req, res) => {
  // TODO: Implement game state retrieval
  res.json({ message: 'Game state endpoint - TODO' });
});

// Player action endpoint
app.post('/api/game/action', (req, res) => {
  // TODO: Implement player action processing
  const { action, playerId } = req.body;
  res.json({ message: 'Action received', action, playerId });
});

// Start server
app.listen(PORT, () => {
  console.log(`🚀 Server running on http://localhost:${PORT}`);
});
