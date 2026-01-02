db = db.getSiblingDB("fitness_activity_db");

db.activities.updateOne(
  { _id: "seed-activity" },
  {
    $setOnInsert: {
      userId: "seed-user",
      type: "RUNNING",
      duration: 30,
      caloriesBurned: 250,
      startTime: new Date(),
      endTime: new Date(),
      metrics: {
        source: "docker-init",
        note: "Seeded on container initialization"
      },
      createdAt: new Date(),
      updatedAt: new Date()
    }
  },
  { upsert: true }
);
