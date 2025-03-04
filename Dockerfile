# Utiliser une image Java de base
FROM openjdk:24-slim

# Définir le répertoire de travail
WORKDIR /app

# Copier le fichier JAR de l'application dans le conteneur
COPY target/images-groupe9-0.0.1-SNAPSHOT.jar /app/app.jar

# Exposer le port sur lequel l'application s'exécute
EXPOSE 8081

# Commande pour exécuter l'application
ENTRYPOINT ["java", "-jar", "app.jar"]