import os

# API tests use the demo token, so run them in demo mode with the mock AI.
os.environ["APP_ENV"] = "demo"
os.environ["AI_PROVIDER"] = "mock"
