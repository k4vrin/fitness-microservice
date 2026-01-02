db = db.getSiblingDB("fitness_ai_db");

db.ai_requests.updateOne(
  { _id: "seed-request" },
  {
    $setOnInsert: {
      prompt: "Seed request for ai-service",
      createdAt: new Date()
    }
  },
  { upsert: true }
);
