#!/bin/bash

APP_NAME="Sprint7"
TARGET_DIR="target"

# Chemin du framework
FRAMEWORK_DIR="../../framework/framework"  # Chemin vers le dossier framework
FRAMEWORK_JAR_SOURCE="$FRAMEWORK_DIR/target/sprint-framework.jar"

# Tomcat
TOMCAT_WEBAPPS="/opt/tomcat11/webapps"

# === Vérification du framework JAR ===
echo "🔍 Vérification du framework JAR..."

if [ ! -f "$FRAMEWORK_JAR_SOURCE" ]; then
    echo "❌ Framework JAR non trouvé dans $FRAMEWORK_JAR_SOURCE"
    echo "💡 Compilez d'abord le framework :"
    echo "   cd ../../framework/framework"
    echo "   mvn clean install"
    exit 1
fi

echo "📦 Génération du WAR avec Maven..."
mvn clean package -Dapp.name="$APP_NAME"
if [ $? -ne 0 ]; then
    echo "❌ Erreur pendant le build Maven"
    exit 1
fi

WAR_FILE="$TARGET_DIR/$APP_NAME.war"

if [ ! -f "$WAR_FILE" ]; then
    echo "❌ WAR non trouvé: $WAR_FILE"
    exit 1
fi

# Déployer
if [ -d "$TOMCAT_WEBAPPS" ]; then
    if cp -f "$WAR_FILE" "$TOMCAT_WEBAPPS/"; then
        echo "✅ WAR déployé vers $TOMCAT_WEBAPPS"
    else
        echo "⚠️ Déploiement Tomcat impossible dans $TOMCAT_WEBAPPS"
        echo "📦 WAR généré: $WAR_FILE"
    fi
else
    echo "⚠️ Répertoire Tomcat non trouvé: $TOMCAT_WEBAPPS"
    echo "📦 WAR généré: $WAR_FILE"
    echo "💡 Copiez-le manuellement dans votre dossier Tomcat/webapps/"
fi

echo ""
echo "========================================="
echo "✅ Déploiement terminé"
echo "📱 Testez: http://localhost:8080/$APP_NAME/"
echo "========================================="
