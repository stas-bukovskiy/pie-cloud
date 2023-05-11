set -e

mongosh <<EOF
db = db.getSiblingDB('$MONGO_DATABASE')

db.createUser({
  user: '$MONGO_USER',
  pwd: '$MONGO_PASSWORD',
  roles: [{ role: 'readWrite', db: '$MONGO_DATABASE' }],
});

db = db.getSiblingDB('$MONGO_TEST_DATABASE')

db.createUser({
  user: '$MONGO_USER',
  pwd: '$MONGO_PASSWORD',
  roles: [{ role: 'readWrite', db: '$MONGO_TEST_DATABASE' }],
});


EOF