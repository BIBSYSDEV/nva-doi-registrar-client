#!/usr/bin/env bash

 aws lambda invoke --function-name orestis-stack-nva-datacite-mds-EventProducer-1P3OPXX6YR2JT \
   --invocation-type Event   \
   --payload file://requestPayload.json \
   --cli-binary-format raw-in-base64-out \
    response.json