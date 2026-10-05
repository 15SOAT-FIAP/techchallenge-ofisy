#!/bin/bash
echo "Initializing LocalStack..."

awslocal sqs create-queue --queue-name techchallenge-ofisy-notifications-queue.fifo --attributes FifoQueue=true,ContentBasedDeduplication=true
echo "SQS FIFO Queue created."

awslocal dynamodb create-table \
    --table-name Notifications \
    --attribute-definitions \
        AttributeName=id,AttributeType=S \
        AttributeName=type,AttributeType=S \
    --key-schema AttributeName=id,KeyType=HASH \
    --global-secondary-indexes \
        "[{\"IndexName\": \"TypeIndex\",\"KeySchema\":[{\"AttributeName\":\"type\",\"KeyType\":\"HASH\"}],\"Projection\":{\"ProjectionType\":\"ALL\"}}]" \
    --billing-mode PAY_PER_REQUEST

echo "DynamoDB Table created."
