pipeline {
    agent any

    options {
        timestamps()
        disableConcurrentBuilds()
    }

    environment {
        APP_NAME = 'ifs25034-p5-pbo2026-springboot-cashflow'
    }

    stages {
        stage('Build & Test') {
            steps {
                sh 'mvn -B clean verify -Djacoco.minimum=0.80'
            }
            post {
                always {
                    junit allowEmptyResults: true, testResults: 'target/surefire-reports/*.xml'
                }
            }
        }

        stage('Jacoco 100%') {
            steps {
                // Gerbang coverage sesuai modul (view/auth dikecualikan di pom.xml)
                sh 'mvn -B jacoco:check@check -Djacoco.minimum=1.00'
            }
        }

        stage('Trivy Scan') {
            steps {
                sh 'trivy fs --exit-code 0 --severity HIGH,CRITICAL --no-progress .'
            }
        }

        stage('Package') {
            steps {
                sh 'mvn -B clean package -Pproduction -DskipTests'
            }
        }

        stage('Publish') {
            steps {
                archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
            }
        }

        stage('Deploy') {
            when {
                branch 'main'
            }
            steps {
                sh '''
                    if [ -n "$DEPLOY_DIR" ]; then
                        mkdir -p "$DEPLOY_DIR"
                        cp target/${APP_NAME}-0.0.1-SNAPSHOT.jar "$DEPLOY_DIR/${APP_NAME}.jar"
                        echo "JAR disalin ke $DEPLOY_DIR. Jalankan ulang service aplikasi."
                    else
                        echo "DEPLOY_DIR tidak diatur; tahap deploy dilewati."
                    fi
                '''
            }
        }
    }
}
