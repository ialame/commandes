# ========== Script setup-dev.sh ==========
#!/bin/bash

echo "🔧 Configuration de l'environnement de développement"

# Vérifier si le fichier de config locale existe
if [ ! -f "src/main/resources/application-local.properties" ]; then
    echo "📝 Création du fichier application-local.properties"
    cp src/main/resources/application-local.properties.example \
       src/main/resources/application-local.properties
    echo "⚠️  Éditez application-local.properties avec vos paramètres locaux"
fi

# Démarrer la base de données de développement
echo "🗄️ Démarrage de MariaDB en développement"
docker-compose -f docker-compose.dev.yml up -d mariadb

# Attendre que la DB soit prête
echo "⏳ Attente de la base de données..."
sleep 10

# Vérifier la connectivité
echo "🔍 Test de connectivité à la base"
docker-compose -f docker-compose.dev.yml exec mariadb \
    mysql -u dev_user -pdev_password -e "SELECT 'DB OK' as status;"

echo "✅ Environnement de développement prêt!"
echo "🚀 Démarrez l'application avec: mvn spring-boot:run"