The goal of this project is to create cheap wristbands that can track your workouts in a simple manner. Exploring the approaches in this paper (https://arxiv.org/pdf/2210.14794) of using HBC’s in addition to the normal IMU’s to aid predicting workout types. The goal is a larger model eventually judging your workout quality (range of motion, speed of reps, etc) with a larger dataset and potentially seeing if you can have your own mini gym coach.

Jul 16, 2024 Creating a document to track progress over phases and to describe the project
Jul 24, 2024 Edited diagram and ordered new parts

Phase 1
Building physical wristbands based on the diagram and sensors included in the diagram below.
Collecting small metrics on how much memory will be required and finding the sweet spot for what frequency we should collect data points at. Find any necessary additions/adjustments to design. Judging battery life needed.

Phase 2
Create a UI app to start warehousing the data on the cloud once the design is finalized. Make multiple wristbands and start collecting months of HIGH QUALITY (good reps) data.

Phase 3
A few months in, gauge how much data is collected and train a smaller model. Cross-validation and batch processing to see if we can already start seeing some patterns. Potentially use the model to test “lower quality” reps and see if it can start tagging them correctly.

Notebook: https://colab.research.google.com/drive/130psW1tL-Hfm8XxKI1DGN7aR_cswvu9E
References on activity recognition ML training:
https://www.ncbi.nlm.nih.gov/pmc/articles/PMC11041400/pdf/ai_v2i1e42337.pdf
References for hardware:
https://cdn-shop.adafruit.com/datasheets/MPR121.pdf
https://www.st.com/en/microcontrollers-microprocessors/stm32wb35cc.html
