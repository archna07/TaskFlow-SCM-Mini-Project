pipeline {
    agent any

    tools {
        // Configure 'JDK 17' and 'Maven 3' in Jenkins Global Tool Configuration if required
        jdk 'JDK17'
        maven 'Maven3'
    }

    options {
        timeout(time: 15, unit: 'MINUTES')
        buildDiscarder(logRotator(numToKeepStr: '10'))
        timestamps()
    }

    stages {
        stage('1. Checkout Source') {
            steps {
                echo 'Checking out source code repository...'
                checkout scm
            }
        }

        stage('2. Environment Validation') {
            steps {
                echo 'Validating Java and Maven environments...'
                script {
                    if (isUnix()) {
                        sh 'java -version'
                        sh 'mvn -version'
                    } else {
                        bat 'java -version'
                        bat 'mvn -version'
                    }
                }
            }
        }

        stage('3. Compile Java Source') {
            steps {
                echo 'Compiling Java 17 application source...'
                script {
                    if (isUnix()) {
                        sh 'mvn clean compile'
                    } else {
                        bat 'mvn clean compile'
                    }
                }
            }
        }

        stage('4. Run Automated Unit Tests') {
            steps {
                echo 'Executing JUnit 5 test suite in headless mode...'
                script {
                    if (isUnix()) {
                        sh 'mvn test'
                    } else {
                        bat 'mvn test'
                    }
                }
            }
        }

        stage('5. Package Application JAR') {
            steps {
                echo 'Packaging executable JAR with manifest...'
                script {
                    if (isUnix()) {
                        sh 'mvn package -DskipTests'
                    } else {
                        bat 'mvn package -DskipTests'
                    }
                }
            }
        }
    }

    post {
        always {
            echo 'Archiving test results and build artifacts...'
            // Publish JUnit XML test results
            junit allowEmptyResults: true, testResults: '**/target/surefire-reports/*.xml'
        }
        success {
            echo 'Build, tests, and packaging succeeded!'
            // Archive the runnable JAR
            archiveArtifacts artifacts: '**/target/*.jar', fingerprint: true, allowEmptyArchive: false
        }
        failure {
            echo 'Pipeline failed! Please check the console output and test reports above.'
        }
        cleanup {
            echo 'Cleaning up workspace artifacts if required.'
        }
    }
}
