#!/usr/bin/env bash
# Dispara o deploy de um Web Service do Render (tipo "Existing Image") apontando para uma imagem exata.
# Uso: RENDER_DEPLOY_HOOK=<url-do-hook> scripts/render-deploy.sh ghcr.io/usuario/repo:sha-<commit>
set -euo pipefail

IMAGE="${1:?informe a imagem, ex.: ghcr.io/usuario/repo:sha-abc123}"

if [ -z "${RENDER_DEPLOY_HOOK:-}" ]; then
  echo "ERRO: secret RENDER_DEPLOY_HOOK nao configurado neste environment do GitHub." >&2
  echo "Configure em Settings > Environments > (staging|production) > Secrets." >&2
  exit 1
fi

# O Render aceita o parametro imgURL (codificado) para servicos baseados em imagem
IMG_ENCODED=$(jq -rn --arg v "$IMAGE" '$v|@uri')

echo "Disparando deploy da imagem $IMAGE ..."
HTTP_CODE=$(curl -sS -o /tmp/render-resposta.json -w '%{http_code}' -X POST \
  "${RENDER_DEPLOY_HOOK}&imgURL=${IMG_ENCODED}")

if [ "$HTTP_CODE" -lt 200 ] || [ "$HTTP_CODE" -ge 300 ]; then
  echo "ERRO: o Render respondeu HTTP $HTTP_CODE" >&2
  cat /tmp/render-resposta.json >&2 || true
  exit 1
fi

echo "Deploy aceito pelo Render (HTTP $HTTP_CODE): $(cat /tmp/render-resposta.json)"
