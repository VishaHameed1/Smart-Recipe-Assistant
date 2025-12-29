**Smart Recipe Assistant 🍳🤖**
An advanced Android application that helps you travel the world through food by recommending recipes based on your ingredients and generating custom culinary guides using Google Gemini AI.

**✨ Key Features**
AI Recipe Generation: Leverages the com.google.genai:google-genai SDK to provide instant ingredients and step-by-step instructions for any dish.

Hands-Free Voice Mode: A specialized cooking mode that reads instructions aloud using Text-to-Speech (TTS) and listens for user commands via Speech Recognition.

Smart File Management: Automatically extracts dish names from AI responses to save recipes as clean, descriptive .txt files (e.g., Classic_Beef_Burger.txt).

Manual Control Panel: Includes dedicated "Next" and "Previous" buttons for users who prefer physical interaction over voice commands.

Ingredient-Based Search: Recommends recipes from a local SQLite database based on the ingredients currently in your fridge.

**🎤 Voice Commands Reference**
The app follows a "Speak -> Listen" loop. You can use the following commands:

"Next": Move to the next cooking step.

"Back" / "Previous": Repeat the previous step.

"Repeat": Listen to the current step again.

"Stop": Exit voice mode and turn off the microphone.

**🛠️ Technical Stack**
Build System: Gradle 8.13.2 (Top-level configuration).

Language: Java 1.8+ (with coreLibraryDesugaring for backward compatibility).

AI SDK: com.google.genai:google-genai:1.32.0.

Asynchronous Support: Guava ListenableFuture for handling AI responses.

Serialization: Jackson Core 2.15.2.

UI Architecture: Material Components 1.10.0 and ConstraintLayout.

**🚀 Getting Started**
Prerequisites
Minimum SDK: API 26 (Android 8.0 Oreo).

Target SDK: API 34 (Android 14).

Gradle Version: 8.13.2.

Java Version: Java 1.8 compatibility enabled.

**Installation**
Clone the repository:

Bash

git clone https://github.com/VishaHameed1/Smart-Recipe-Assistant.git
Open the project in Android Studio.

Configure your Gemini API Key in SearchResult.java.

Sync Gradle. The packaging block in the module-level build.gradle is pre-configured to handle META-INF duplicate resource errors.

**📂 Project Components**
SplashActivity.java: Professional entry point with branding and initialization.

SearchResult.java: Processes Gemini AI outputs, handles TTS/Speech Recognition loops, and manages dynamic file saving based on the dish name.

SavedAiRecipesActivity.java: A dedicated manager to list, view, and delete recipes stored in internal storage.

**Author**
**_VISHA HAMEED_**/https://github.com/VishaHameed1 Lead Developer ## License GNU GPLv3