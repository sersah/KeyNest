# KeyNest 🔑 & 🔒
![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white) ![JavaFX](https://img.shields.io/badge/javafx-%23FF0000.svg?style=for-the-badge&logo=javafx&logoColor=white) ![CSS3](https://img.shields.io/badge/css3-%231572B6.svg?style=for-the-badge&logo=css3&logoColor=white)  
KeyGenerator and KeyManager combined!  
Generate secure passwords and store them in an offline encrypted file! 


**Features  ✨**
---------
- Customizable Password Length
- **Optional Character Types**:  
  - Special characters (e.g., `@`, `#`, `!`)  
  - Uppercase letters (e.g., `A`, `B`, `C`)  
  - Lowercase letters (e.g., `a`, `b`, `c`)  
  - Digits (e.g., `0`, `1`, `2`)

- Create an offline encrypted database to store your accounts data securely
- Choose a Master Password to create and encrypt the entire database
- Easily add, edit and remove the accounts



**Specification ⚙️**
--------
- Strong, random password generator using `SecureRandom`
- GUI developed with JavaFX SceneBuilder
- AES encryption
- CSV database format
- Maven support for dependencies and automation

**Screenshots 🖼️**
---------

Generator/Main view | KeyManager login view
:-------------------------:|:-------------------------:
![Preview](screenshots/GUImain.PNG)  |  ![Preview](screenshots/KeyManagerLogin.PNG)


KeyManager view (passwords hidden) | KeyManager view (passwords shown)
:-------------------------:|:-------------------------:
![Preview](screenshots/KeyManagerNOPW.PNG)  |  ![Preview](screenshots/KeyManagerPW.PNG)
