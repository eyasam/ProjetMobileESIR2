# 📱 Projet Android - Jeu Multijoueur en Bluetooth (ESIR2)

## 🎯 Objectif du projet

Créer un jeu mobile Android où deux téléphones peuvent se connecter entre eux via Bluetooth pour s’affronter sur une série de mini-jeux. Le but est de marquer un maximum de points sur 3 défis choisis au hasard, puis de voir qui gagne.  
À la fin, une musique de victoire retentit pour le gagnant, et une musique de défaite pour l’autre. 😅

---

## ⚙️ Fonctionnalités principales

- Écran d’accueil + lancement du jeu
- **6 mini-jeux** répartis en 3 catégories :
  - Capteurs (accéléromètre, gyroscope)
  - Mouvement écran (tactile, glisser/déposer)
  - QCM (Quiz)
- **Mode solo** :
  - 3 défis aléatoires à la suite
  - Score final affiché à la fin
- **Mode multijoueur Bluetooth** :
  - Détection des joueurs proches
  - Connexion Bluetooth (RFCOMM)
  - Lancement synchronisé des défis
  - Échange des scores
  - Détermination du gagnant
  - Musique de fin 🎵
- **Mode entraînement** :
  - Accès libre à tous les mini-jeux
  - Aucun score final affiché
  - Idéal pour tester les jeux ou s'améliorer sans pression

---

## 🎮 Les mini-jeux

| Nom du jeu            | Catégorie              | Description rapide                            | Modes disponibles      |
|-----------------------|------------------------|-----------------------------------------------|------------------------|
| Cut the Fruit         | Mouvement écran        | Tu dois couper les fruits, éviter les bombes  | Solo / Multi / Entraînement |
| Devine le mot         | Question/réponse       | Énigmes à résoudre en tapant le mot           | Solo / Multi / Entraînement |
| Quiz (QCM)            | Question/réponse       | Choisir la bonne réponse à un QCM illustré    | Solo / Multi / Entraînement |
| Gyroscope Challenge   | Capteurs (orientation) | Trouver un angle précis avec le gyroscope     | Solo / Multi / Entraînement |
| Shake Challenge       | Capteurs (accéléro)    | Secouer le téléphone à fond pendant un chrono | Solo / Multi / Entraînement |
| Snake                 | Mouvement écran        | Jeu Snake classique, plus tu tiens, mieux c’est | Solo / Entraînement uniquement |

---

## 🧱 Technologies utilisées

- Android (Java)
- Bluetooth (RFCOMM : serveur + client)
- MediaPlayer pour les sons
- SharedPreferences pour les scores
- SensorManager pour gyroscope et accéléromètre
- Interface graphique en XML
- Quelques animations custom 🎨

---

## 🔧 Installation rapide

1. Cloner ce dépôt
2. Ouvrir dans Android Studio
3. Compiler et lancer l’app sur **deux téléphones Android**
4. Activer le Bluetooth sur chaque appareil
5. Accorder les permissions demandées
6. C’est parti ! 🎮

---

## 📌 Notes personnelles

•⁠  ⁠Pas mal de galère avec les permissions Bluetooth (surtout avec les versions récentes d’Android)
•⁠  ⁠La synchro entre les 2 appareils, c’était un peu technique mais ça marche bien maintenant
•⁠  ⁠Les sons à la fin, c’est pour l’ambiance 😎
- Le mode entraînement est bien utile pour tester les défis sans pression

---

## 👨‍💻 Projet réalisé par

Projet réalisé à l’**ESIR** par **EYA SAMMARI** et **AYAT ALLAH EL ANOUAR**, dans le cadre du module *Programmation Mobile* ESIR2 SI

---

## 📂 Dossier à rendre

Contenu :
- Code Java complet
- Ressources (sons, images, layouts XML)
- README.md
