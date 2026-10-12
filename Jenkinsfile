// Pipeline CI/CD mẫu cho Jenkins (Declarative Pipeline).
// Minh họa các bước: lấy mã nguồn -> build & test -> đóng gói -> build Docker image.
pipeline {
    agent any

    tools {
        // Tên này phải trùng cấu hình trong Manage Jenkins > Tools
        jdk 'jdk-21'
        maven 'maven-3.9'
    }

    options {
        timestamps()
        buildDiscarder(logRotator(numToKeepStr: '10'))
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build & Unit Test') {
            steps {
                sh 'mvn -B clean test'
            }
            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                }
            }
        }

        stage('Package') {
            steps {
                sh 'mvn -B -DskipTests package'
                archiveArtifacts artifacts: 'target/payment-wallet-demo.jar', fingerprint: true
            }
        }

        stage('Docker Build') {
            steps {
                sh 'docker build -t sonddd/payment-wallet-demo:${BUILD_NUMBER} .'
            }
        }
    }

    post {
        success { echo 'Pipeline thành công ✅' }
        failure { echo 'Pipeline thất bại ❌' }
    }
}
