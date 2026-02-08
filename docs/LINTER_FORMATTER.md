# Configuration du Linter et Formateur

Ce document explique comment utiliser le linter et le formateur configurés pour ce projet.

## 📋 Vue d'ensemble

Le projet est configuré avec les outils suivants :

- **Spotless** : Formateur de code automatique (Google Java Format)
- **CheckStyle** : Linter pour vérifier la qualité du code
- **VS Code** : Formatage automatique à la sauvegarde (Ctrl+S)

## 🚀 Installation des Extensions VS Code

Les extensions suivantes sont recommandées :

1. **Language Support for Java (Red Hat)** - Support Java dans VS Code
2. **IntelliCode** - Autocomplétion intelligente
3. **Checkstyle for Java** - Intégration CheckStyle
4. **SonarLint** - Analyse de qualité supplémentaire

VS Code vous proposera d'installer ces extensions automatiquement. Acceptez les recommandations.

## 📝 Formatage Automatique

### À la Sauvegarde (Ctrl+S)

Le formatage s'applique automatiquement à la sauvegarde grâce à la configuration dans `.vscode/settings.json` :

```json
"[java]": {
    "editor.formatOnSave": true,
    "editor.defaultFormatter": "redhat.java"
}
```

### Vérifier rapidement

Appuyez sur **Ctrl+Shift+I** dans VS Code pour formater le fichier courant.

## 🔍 Utilisation du Linter

### Vérifier les violations

```bash
mvn checkstyle:check
```

### Générer un rapport HTML

```bash
mvn checkstyle:checkstyle
```
Le rapport sera généré dans `target/site/checkstyle.html`

## 🧹 Utilisation du Formateur (CLI)

### Formater tout le code

```bash
mvn spotless:apply
```

### Vérifier le formatage sans appliquer les changements

```bash
mvn spotless:check
```

## 📜 Script Interactif

Un script `format-lint.sh` est disponible pour faciliter les opérations :

```bash
chmod +x format-lint.sh
./format-lint.sh
```

Le script propose un menu avec plusieurs options :
1. Formater le code
2. Vérifier les violations
3. Formater + Vérifier
4. Nettoyer et compiler
5. Quitter

## ⚙️ Configuration

### CheckStyle (checkstyle.xml)

Le fichier `checkstyle.xml` contient les règles de style appliquées au projet :

- **Longueur des lignes** : 120 caractères max
- **Longueur des méthodes** : 200 lignes max
- **Nombre de paramètres** : 8 max
- **Convention de nommage** : camelCase pour les variables/méthodes
- **Imports** : organisés et sans wildcard
- **Whitespace** : strict

### Google Java Format (Spotless)

Spotless utilise le style Google Java Format qui applique :
- Indentation de 2 espaces
- Formatage automatique des blocs
- Gestion des espaces et importation

## 💡 Conseils

1. **Exécutez régulièrement** `mvn spotless:apply` pour garder le code formaté
2. **Vérifiez les violations** avant de pusher : `mvn checkstyle:check`
3. **Utilisez l'IDE** : le formatage automatique à la sauvegarde est votre meilleur ami
4. **Ignorez certaines règles** : si une règle est trop stricte, modifiez `checkstyle.xml`

## 📊 Intégration CI/CD

Pour une intégration en CI/CD (GitHub Actions, Jenkins, etc.), ajoutez :

```bash
mvn clean compile checkstyle:check spotless:check
```

## ❓ Troubleshooting

### Le formatage ne s'applique pas

1. Vérifiez que VS Code a redhat.java d'installé
2. Recharger VS Code : Ctrl+Shift+P → "Reload Window"
3. Vérifiez que .vscode/settings.json est correct

### Les violations CheckStyle ne s'affichent pas

1. Assurez-vous que vous avez l'extension Checkstyle pour Java
2. Recompile le projet : `mvn clean compile`
3. Vérifiez les logs de VS Code : Ctrl+Shift+U

### Conflit entre formatages

Si plusieurs formateurs s'exécutent, vérifiez `.vscode/settings.json` et gardez seulement `redhat.java` comme formateur par défaut.
