# KeyNest 🔑 & 🔒
![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white) ![JavaFX](https://img.shields.io/badge/javafx-%23FF0000.svg?style=for-the-badge&logo=javafx&logoColor=white) ![CSS3](https://img.shields.io/badge/css3-%231572B6.svg?style=for-the-badge&logo=css3&logoColor=white)  
KeyGenerator and KeyManager combined!  
Generate secure passwords and store them in an offline encrypted file! 


**Features  ✨**
---------
- Customizable Password Length
- **Configurable Character Types**:  
  - Special characters (e.g., `@`, `#`, `!`)  
  - Uppercase letters (e.g., `A`, `B`, `C`)  
  - Lowercase letters (e.g., `a`, `b`, `c`)  
  - Digits (e.g., `0-9`)

- **Offline Encrypted Vault**: Stores credentials locally in an AES-encrypted database file (`~/keyholder`).
- **Master Password Protection**: Derives a 128-bit AES key from the user's master password via `SHA-256`.
- **Full CRUD Management**: Add, edit, sort, and remove saved accounts with toggleable password visibility.


**Specification ⚙️**
--------
- Cryptographically strong password generation using `SecureRandom`
- SHA-256 message digest for master key derivation `SecretKeySpec`
- GUI developed with JavaFX SceneBuilder
- AES for vault encryption
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

---

## 🔄 Flow Diagram

![Flow Diagram](screenshots/flow_diagram.svg)

---
