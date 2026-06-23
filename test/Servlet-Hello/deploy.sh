#!/bin/bash

APP_NAME="Spint2"
SRC_DIR="src/main/java"
WEB_DIR="src/main/webapp"
BUILD_DIR="build"
LIB_DIR="lib"

# Chemins des frameworks (à adapter selon votre structure)
FRAMEWORK_DIR="../../framework/framework"  # Chemin vers le dossier framework
FRAMEWORK_JAR_SOURCE="$FRAMEWORK_DIR/target/sprint-framework-1.0-SNAPSHOT.jar"
FRAMEWORK_JAR_TARGET="$LIB_DIR/sprint-framework.jar"

# Tomcat
TOMCAT_WEBAPPS="/opt/tomcat11/webapps"

# Jakarta Servlet API
JAKARTA_SERVLET_API="$LIB_DIR/servlet-api.jar"

# === Vérification et copie automatique du framework JAR ===
echo "🔍 Vérification du framework JAR..."

if [ -f "$FRAMEWORK_JAR_SOURCE" ]; then
    echo "📦 Copie du framework JAR fraîchement compilé..."
    cp "$FRAMEWORK_JAR_SOURCE" "$FRAMEWORK_JAR_TARGET"
    echo "📋 Framework JAR copié vers $FRAMEWORK_JAR_TARGET"
else
    echo "❌ Framework JAR non trouvé dans $FRAMEWORK_JAR_SOURCE"
    echo "💡 Compilez d'abord le framework :"
    echo "   cd ../../framework/framework"
    echo "   mvn clean install"
    exit 1
fi

# Vérifier si Jakarta API existe
if [ ! -f "$JAKARTA_SERVLET_API" ]; then
    echo "❌ Erreur: $JAKARTA_SERVLET_API manquant"
    echo "💡 Téléchargez-le :"
    echo "   cd $LIB_DIR"
    echo "   wget https://repo1.maven.org/maven2/jakarta/servlet/jakarta.servlet-api/6.0.0/jakarta.servlet-api-6.0.0.jar"
    exit 1
fi

# Nettoyage
echo "🧹 Nettoyage du dossier build..."
rm -rf $BUILD_DIR
mkdir -p $BUILD_DIR/WEB-INF/classes
mkdir -p $BUILD_DIR/WEB-INF/lib

# Compiler les classes Java du projet test (si vous en avez)
if [ -d "$SRC_DIR" ] && [ "$(ls -A $SRC_DIR 2>/dev/null)" ]; then
    echo "📝 Compilation des sources..."
    find $SRC_DIR -name "*.java" > sources.txt 2>/dev/null
    if [ -s sources.txt ]; then
        javac -cp "$FRAMEWORK_JAR_TARGET:$JAKARTA_SERVLET_API" \
              -d $BUILD_DIR/WEB-INF/classes @sources.txt
        if [ $? -eq 0 ]; then
            echo "✅ Compilation réussie"
        else
            echo "⚠️ Erreurs de compilation (ignorées)"
        fi
    fi
    rm -f sources.txt
fi

# Copier le framework JAR
cp $FRAMEWORK_JAR_TARGET $BUILD_DIR/WEB-INF/lib/

# Copier web.xml (NE CREE PLUS, COPIE UNIQUEMENT SI EXISTANT)
if [ -d "$WEB_DIR" ]; then
    cp -r $WEB_DIR/* $BUILD_DIR/
else
    echo "❌ Erreur: $WEB_DIR n'existe pas"
    echo "💡 Créez le dossier src/main/webapp/WEB-INF/ et mettez votre web.xml dedans"
    exit 1
fi

# Créer le WAR
echo "📦 Création du fichier WAR..."
cd $BUILD_DIR
jar -cvf $APP_NAME.war * > /dev/null
cd ..

# Déployer
if [ -d "$TOMCAT_WEBAPPS" ]; then
    if cp -f $BUILD_DIR/$APP_NAME.war $TOMCAT_WEBAPPS/; then
        echo "✅ WAR déployé vers $TOMCAT_WEBAPPS"
    else
        echo "⚠️ Déploiement Tomcat impossible dans $TOMCAT_WEBAPPS"
        echo "📦 WAR généré: $BUILD_DIR/$APP_NAME.war"
    fi
else
    echo "⚠️ Répertoire Tomcat non trouvé: $TOMCAT_WEBAPPS"
    echo "📦 WAR généré: $BUILD_DIR/$APP_NAME.war"
    echo "💡 Copiez-le manuellement dans votre dossier Tomcat/webapps/"
fi

echo ""
echo "========================================="
echo "✅ Déploiement terminé"
echo "📱 Testez: http://localhost:8080/$APP_NAME/"
echo "========================================="
