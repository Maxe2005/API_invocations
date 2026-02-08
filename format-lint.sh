#!/bin/bash

# Script de formatage et linting du projet

set -e

echo "====================================="
echo "   Formatage et Linting du Projet"
echo "====================================="
echo ""

# Vérifier que Maven est disponible
if ! command -v mvn &> /dev/null; then
    echo "❌ Maven n'est pas installé. Veuillez installer Maven."
    exit 1
fi

# Menu principal
echo "Choisissez une action:"
echo "1) Formater le code (Spotless)"
echo "2) Vérifier les violations (CheckStyle)"
echo "3) Formater + Vérifier"
echo "4) Nettoyer et compiler"
echo "5) Quitter"
echo ""
read -p "Sélectionnez une option (1-5): " choice

case $choice in
    1)
        echo ""
        echo "🔧 Formatage du code avec Spotless..."
        mvn spotless:apply
        echo "✅ Formatage terminé!"
        ;;
    2)
        echo ""
        echo "🔍 Vérification avec CheckStyle..."
        mvn checkstyle:check
        echo "✅ Vérification terminée!"
        ;;
    3)
        echo ""
        echo "🔧 Formatage du code avec Spotless..."
        mvn spotless:apply
        echo "✅ Formatage terminé!"
        echo ""
        echo "🔍 Vérification avec CheckStyle..."
        mvn checkstyle:check
        echo "✅ Vérification terminée!"
        ;;
    4)
        echo ""
        echo "🧹 Nettoyage et compilation..."
        mvn clean compile -q
        echo "✅ Compilation terminée!"
        ;;
    5)
        echo "Au revoir!"
        exit 0
        ;;
    *)
        echo "❌ Option invalide"
        exit 1
        ;;
esac
