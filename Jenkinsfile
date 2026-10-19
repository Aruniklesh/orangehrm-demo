pipeline {
    agent any

    // Names must match Manage Jenkins -> Tools
    tools {
        jdk 'JDK21'
        maven 'Maven3'
    }

    environment {
        // Secret text credentials stored in Jenkins (never in Git)
        ORANGEHRM_USERNAME = credentials('orangehrm-username')
        ORANGEHRM_PASSWORD = credentials('orangehrm-password')
    }

    options {
        timeout(time: 15, unit: 'MINUTES')
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Test') {
            steps {
                // Jenkins has no desktop, so run Chrome headless
                bat 'mvn clean test -Dheadless=true'
            }
        }
    }

    post {
        always {
            junit allowEmptyResults: true, testResults: 'target/surefire-reports/TEST-*.xml'
        }
    }
}
