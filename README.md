# Cyber Runner 👾

_Plongez dans un labyrinthe numérique et survivez à la poursuite ! Un jeu d'arcade rétro-futuriste inspiré de Pac-Man._

Bienvenue dans l'univers de **Cyber Runner** ! Un jeu où réflexes, stratégie et sang-froid sont vos meilleurs alliés pour naviguer dans des labyrinthes infestés d'intelligences artificielles hostiles.

---

### 📜 Table des Matières

* [À propos du projet](#à-propos-du-projet)
* [Fonctionnalités Clés](#fonctionnalités-clés-sparkles)
* [Construit Avec](#construit-avec-hammer_and_wrench)
* [Pour Commencer](#pour-commencer-rocket)
    * [Prérequis](#prérequis)
    * [Installation](#installation)
* [Comment Jouer](#comment-jouer-video_game)
* [Contribuer](#contribuer-handshake)
* [Licence](#licence-page_facing_up)
* [Contact](#contact-mailbox)

---

### 📝 À propos du projet

**Cyber Runner** est une réinterprétation moderne et dynamique du classique intemporel Pac-Man, développée en Java avec l'API Swing. Le jeu vous plonge dans une ambiance cyberpunk où vous incarnez un "Runner" dont la mission est de collecter des fragments de données (les points) tout en échappant à des programmes de sécurité (les ennemis).

Avec ses 20 niveaux à la difficulté croissante, son IA ennemie avancée et ses power-ups stratégiques, Cyber Runner offre une expérience de jeu à la fois nostalgique et renouvelée. Le projet a été conçu pour être non seulement amusant, mais aussi robuste, maintenable et bien documenté, suivant les meilleures pratiques de développement logiciel.

---

### ✨ Fonctionnalités Clés

* **🕹️ Gameplay Classique Revisité :** Vivez l'excitation du jeu de labyrinthe avec une touche de modernité et une difficulté qui s'adapte à votre progression.
* **🌐 20 Niveaux Uniques :** Parcourez 20 missions de plus en plus complexes, générées de manière procédurale pour une rejouabilité infinie.
* **🤖 IA Ennemie Stratégique :** Affrontez 4 types d'ennemis aux comportements distincts pour des défis variés :
    * **Hunter (Chasseur) 🟥 :** Vous traque sans relâche.
    * **Ambusher (Embusqué) 🟪 :** Tente de vous couper la route.
    * **Flanker (Flanqueur) 🟧 :** Coordonne ses attaques avec les autres ennemis.
    * **Roamer (Rôdeur) 🟫 :** Patrouille de manière imprévisible.
* **⚡ Power-Ups Dynamiques :** Renversez le cours du jeu avec trois power-ups décisifs :
    * **Super Pellet 🟡 :** Rendez les ennemis vulnérables et augmentez votre score.
    * **Freeze ❄️ :** Gelez temporairement tous les ennemis sur place.
    * **Shield 🛡️ :** Protégez-vous d'une collision fatale.
* **🏆 Système de Meilleurs Scores :** Enregistrez vos exploits et hissez-vous au sommet du "Panthéon des Hackers".
* **👤 Profil Joueur :** Suivez vos statistiques de jeu : score total, power-ups collectés, ennemis vaincus et niveaux terminés.
* **🎨 Thèmes Visuels Personnalisables :** Changez l'ambiance du jeu avec 3 thèmes d'interface uniques :
    * `Cyber Néon` (par défaut)
    * `Noyau Volcanique`
    * `Matrice Arctique`
* **✨ Interface Animée et Soignée :** Profitez de menus et de boutons animés pour une expérience utilisateur immersive.

---

### 🛠️ Construit Avec

Ce projet a été entièrement réalisé avec des technologies robustes et éprouvées :

* [Java](https://www.java.com/) - Le langage de programmation principal.
* [Swing (Java API)](https://docs.oracle.com/javase/8/docs/api/javax/swing/package-summary.html) - Pour toute l'interface graphique.

---

### 🚀 Pour Commencer

Pour obtenir une copie locale du projet et commencer à jouer, suivez ces étapes simples.

#### Prérequis

Assurez-vous d'avoir le **Java Development Kit (JDK)** (version 8 ou supérieure) installé sur votre machine.

* [Comment installer le JDK](https://docs.oracle.com/en/java/javase/17/install/overview-jdk-installation.html)

#### Installation

1.  **Clonez le dépôt**
    ```sh
    git clone [https://github.com/technerdsam/Pacman-version-rtn.git]
    ```
2.  **Naviguez vers le répertoire du projet**
    ```sh
    cd chemin/vers/le/projet
    ```
3.  **Compilez le code source**
    (En supposant que le fichier `PacManGame.java` est à la racine de votre dossier `src`)
    ```sh
    javac PacManGame.java
    ```
4.  **Exécutez le jeu**
    ```sh
    java PacManGame
    ```
Le jeu devrait maintenant se lancer. Amusez-vous bien ! 🎉

---

### 🎮 Comment Jouer

Les règles de **Cyber Runner** sont simples à comprendre, mais difficiles à maîtriser.

* **Déplacement :** Utilisez les **flèches directionnelles** ( haut, bas, gauche, droite) pour déplacer votre Runner dans le labyrinthe.
* **Objectif :** Collectez tous les petits points blancs (`fragments de données`) sur la grille pour terminer le niveau et passer au suivant.
* **Évitez les ennemis :** Tout contact avec un ennemi met fin à la partie, à moins que vous ne soyez sous l'effet d'un power-up.
* **Utilisez les Power-Ups :** Collectez les objets spéciaux pour prendre l'avantage. Ils sont la clé de la victoire dans les niveaux les plus difficiles.
* **Pause :** Appuyez sur la touche `P` pour mettre le jeu en pause (fonctionnalité à venir).

---

### 🤝 Contribuer

Les contributions sont ce qui rend la communauté open source si incroyable. Toute contribution que vous apporterez sera **grandement appréciée**.

Si vous avez une suggestion pour améliorer le jeu, n'hésitez pas à "forker" le dépôt et à créer une "pull request". Vous pouvez également ouvrir une "issue" avec le tag "enhancement".

1.  Forkez le Projet
2.  Créez votre branche de fonctionnalité (`git checkout -b feature/AmazingFeature`)
3.  Commitez vos changements (`git commit -m 'Add some AmazingFeature'`)
4.  Poussez vers la branche (`git push origin feature/AmazingFeature`)
5.  Ouvrez une Pull Request

N'oubliez pas de donner une étoile au projet si vous l'avez apprécié ! ⭐

---

### 📄 Licence

Distribué sous la licence Creative Commons. Voir `LICENSE` pour plus d'informations.

---

### 📫 Contact

**Samyn-Antoy ABASSE**

* **Profil GitHub :** [@TechNerdSam](https://github.com/TechNerdSam)
* **Email :** samynantoy@gmail.com

N'hésitez pas à me contacter pour toute question ou suggestion !
