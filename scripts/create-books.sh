for i in {1..50}; do
  bookId="B$(printf "%03d" $((RANDOM % 999 + 1)))"
  bookName="Book$((RANDOM % 1000))"
  authorName="Author$((RANDOM % 100))"
  echo "Request $i/50 - bookId: $bookId"
  curl -X 'POST' 'http://127.0.0.1:55104/api/books' \
    -H 'accept: */*' \
    -H 'Content-Type: application/json' \
    -d "{\"bookId\": \"$bookId\", \"bookName\": \"$bookName\", \"authorName\": \"$authorName\"}"
  echo -e "\n---"
  sleep 1
done