#!/bin/bash
set -euo pipefail

export AWS_PAGER=""

DLQ_URL=$(awslocal sqs create-queue \
  --queue-name ofisy-service-order-dlq.fifo \
  --attributes FifoQueue=true \
  --query QueueUrl --output text)

DLQ_ARN=$(awslocal sqs get-queue-attributes \
  --queue-url "$DLQ_URL" \
  --attribute-names QueueArn \
  --query Attributes.QueueArn --output text)

QUEUE_URL=$(awslocal sqs create-queue \
  --queue-name ofisy-service-order.fifo \
  --attributes "{\"FifoQueue\":\"true\",\"RedrivePolicy\":\"{\\\"deadLetterTargetArn\\\":\\\"$DLQ_ARN\\\",\\\"maxReceiveCount\\\":\\\"5\\\"}\"}" \
  --query QueueUrl --output text)

QUEUE_ARN=$(awslocal sqs get-queue-attributes \
  --queue-url "$QUEUE_URL" \
  --attribute-names QueueArn \
  --query Attributes.QueueArn --output text)

EXECUTION_TOPIC_ARN=$(awslocal sns create-topic \
  --name ofisy-execution-events.fifo \
  --attributes FifoTopic=true \
  --query TopicArn --output text)

awslocal sns subscribe \
  --topic-arn "$EXECUTION_TOPIC_ARN" \
  --protocol sqs \
  --notification-endpoint "$QUEUE_ARN" \
  --attributes '{"RawMessageDelivery":"true","FilterPolicyScope":"MessageBody","FilterPolicy":"{\"type\":[\"ExecutionStarted\",\"AllExecutionsCompleted\"]}"}'

BILLING_TOPIC_ARN=$(awslocal sns create-topic \
  --name ofisy-billing-events.fifo \
  --attributes FifoTopic=true \
  --query TopicArn --output text)

awslocal sns subscribe \
  --topic-arn "$BILLING_TOPIC_ARN" \
  --protocol sqs \
  --notification-endpoint "$QUEUE_ARN" \
  --attributes '{"RawMessageDelivery":"true","FilterPolicyScope":"MessageBody","FilterPolicy":"{\"type\":[\"QuoteSentForApproval\",\"QuoteApproved\",\"QuoteCancelled\"]}"}'

STOCK_TOPIC_ARN=$(awslocal sns create-topic \
  --name ofisy-stock-events.fifo \
  --attributes FifoTopic=true \
  --query TopicArn --output text)

awslocal sns subscribe \
  --topic-arn "$STOCK_TOPIC_ARN" \
  --protocol sqs \
  --notification-endpoint "$QUEUE_ARN" \
  --attributes '{"RawMessageDelivery":"true","FilterPolicyScope":"MessageBody","FilterPolicy":"{\"type\":[\"StockRefunded\"]}"}'
  
SERVICE_ORDER_TOPIC_ARN=$(awslocal sns create-topic \
  --name ofisy-service-order-events.fifo \
  --attributes FifoTopic=true \
  --query TopicArn --output text)

INSPECT_QUEUE_URL=$(awslocal sqs create-queue \
  --queue-name ofisy-service-order-events-inspect.fifo \
  --attributes FifoQueue=true \
  --query QueueUrl --output text)

INSPECT_QUEUE_ARN=$(awslocal sqs get-queue-attributes \
  --queue-url "$INSPECT_QUEUE_URL" \
  --attribute-names QueueArn \
  --query Attributes.QueueArn --output text)

awslocal sns subscribe \
  --topic-arn "$SERVICE_ORDER_TOPIC_ARN" \
  --protocol sqs \
  --notification-endpoint "$INSPECT_QUEUE_ARN" \
  --attributes '{"RawMessageDelivery":"true"}'
