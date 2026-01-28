#!/bin/bash

set -e
set -u

function create_user_and_database() {
    local database=$1
    local user=$2
    local password=$3
    echo "Creating database '$database' for user '$user'"
    psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" <<-EOSQL
        CREATE USER $user WITH PASSWORD '$password';
        CREATE DATABASE $database;
        GRANT ALL PRIVILEGES ON DATABASE $database TO $user;
EOSQL
}

# Create SIMs database and user
create_user_and_database "syte_db" "syte_user" "syte_pwd"

# Create Keycloak database and user
create_user_and_database "keycloak_db" "keycloak_user" "keycloak_password"

echo "Multiple databases created successfully"