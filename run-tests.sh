#!/bin/bash

# Test Suite Setup and Execution Script
# Cette script configure et exécute la suite de tests complète

set -e

echo "========================================="
echo "   API Invocations - Test Suite Setup"
echo "========================================="
echo ""

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Functions
print_status() {
    echo -e "${GREEN}✓${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}⚠${NC} $1"
}

print_error() {
    echo -e "${RED}✗${NC} $1"
}

# Check prerequisites
echo "1. Vérification des prérequis..."
echo ""

# Check Java
if ! command -v java &> /dev/null; then
    print_error "Java n'est pas installé"
    exit 1
fi

JAVA_VERSION=$(java -version 2>&1 | grep -oP '(?<=version ")[^"]*' | cut -d. -f1)
if [ "$JAVA_VERSION" -ge 21 ]; then
    print_status "Java 21 trouvé"
else
    print_warning "Java 21 recommandé (trouvé: Java $JAVA_VERSION)"
fi

# Check Maven
if ! command -v mvn &> /dev/null; then
    print_error "Maven n'est pas installé"
    exit 1
fi
print_status "Maven trouvé"

# Check Docker (for MongoDB)
if command -v docker &> /dev/null; then
    print_status "Docker trouvé"
else
    print_warning "Docker non trouvé - MongoDB local sera utilisé si disponible"
fi

echo ""
echo "2. Configuration du projet..."
echo ""

# Check if in project root
if [ ! -f "pom.xml" ]; then
    print_error "pom.xml non trouvé - exécutez depuis la racine du projet"
    exit 1
fi
print_status "Projet Maven trouvé"

# Check if test files exist
if [ ! -d "src/test/java" ]; then
    print_error "Répertoire src/test/java non trouvé"
    exit 1
fi
print_status "Tests trouvés"

echo ""
echo "3. Lancement des tests..."
echo ""

# Run tests
echo "Exécution de: mvn clean test"
echo ""

if mvn clean test; then
    print_status "Tous les tests sont passés!"
else
    print_error "Certains tests ont échoué"
    echo ""
    echo "Pour déboguer:"
    echo "  - Vérifiez les logs dans target/surefire-reports/"
    echo "  - Exécutez: mvn test -DfailIfNoTests=false -Dtest=<NomDuTest>"
    echo "  - Assurez-vous que MongoDB est disponible"
    exit 1
fi

echo ""
echo "4. Génération du rapport de couverture..."
echo ""

if mvn jacoco:report; then
    print_status "Rapport Jacoco généré"
    echo ""
    echo "Rapport disponible à: target/site/jacoco/index.html"
else
    print_warning "Jacoco n'est pas configuré"
fi

echo ""
echo "========================================="
echo "   Résumé de l'Exécution"
echo "========================================="
echo ""
print_status "Configuration complète"
print_status "Tests exécutés avec succès"
echo ""
echo "Prochaines étapes:"
echo "  1. Consultez TESTING_GUIDE.md pour plus de détails"
echo "  2. Consultez TEST_DEPENDENCIES.md pour les dépendances optionnelles"
echo "  3. Exécutez: open target/site/jacoco/index.html (pour la couverture)"
echo ""
