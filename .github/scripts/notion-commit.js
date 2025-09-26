const { Client } = require('@notionhq/client');

// 환경 변수 로깅
console.log('Environment variables:');
console.log('NOTION_KEY:', process.env.NOTION_KEY ? 'Set' : 'Not set');
console.log('NOTION_DATABASE_ID:', process.env.NOTION_DATABASE_ID);
console.log('GITHUB_REPOSITORY:', process.env.GITHUB_REPOSITORY);
console.log('GITHUB_REF:', process.env.GITHUB_REF);

const notion = new Client({ auth: process.env.NOTION_KEY });

async function createCommitRecord(commitData) {
  try {
    console.log('Received commit data:', JSON.stringify(commitData, null, 2));
    
    const response = await notion.pages.create({
      parent: { database_id: process.env.NOTION_DATABASE_ID },
      properties: {
        '커밋 메시지': {
          title: [
            {
              text: {
                content: (commitData.tags && commitData.tags.length > 0) 
                  ? `[${commitData.tags.join(', ')}] ${commitData.message.split('\n')[0]}`
                  : commitData.message.split('\n')[0] || 'No message'
              }
            }
          ]
        },
        '커밋 해시': {
          rich_text: [
            {
              text: {
                content: commitData.id || 'N/A'
              }
            }
          ]
        },
        '작성자': {
          rich_text: [
            {
              text: {
                content: commitData.author.name || 'Unknown'
              }
            }
          ]
        },
        '날짜': {
          date: {
            start: commitData.timestamp || new Date().toISOString()
          }
        },
        '브랜치': {
          rich_text: [
            {
              text: {
                content: process.env.GITHUB_REF?.split('/').pop() || 'main'
              }
            }
          ]
        },
        '변경 파일': {
          rich_text: [
            {
              text: {
                content: [
                  ...(commitData.added || []),
                  ...(commitData.modified || []),
                  ...(commitData.removed || [])
                ].join('\n') || 'None'
              }
            }
          ]
        },
        '커밋 링크': {
          url: `https://github.com/${process.env.GITHUB_REPOSITORY}/commit/${commitData.id}`
        },
        '저장소': {
          rich_text: [
            { text: { content: process.env.GITHUB_REPOSITORY || 'N/A' } }
          ]
        },
        '변경 통계': {
          rich_text: [
            { 
              text: { 
                content: `++${(commitData.added || []).length} --${(commitData.removed || []).length} ±${(commitData.modified || []).length}`
              }
            }
          ]
        }
      },
      children: [
        {
          object: 'block',
          type: 'paragraph',
          paragraph: {
            rich_text: [
              {
                type: 'text',
                text: {
                  content: commitData.message.split('\n').slice(1).join('\n').trim() || 'No additional content'
                }
              }
            ]
          }
        }
      ]
    });
    console.log('Notion record created successfully:', response.url);
  } catch (error) {
    console.error('Error creating Notion record:', error);
    console.error('Error details:', {
      message: error.message,
      code: error.code,
      status: error.status,
      body: error.body
    });
    throw error;
  }
}

// GitHub Actions에서 전달된 커밋 데이터 파싱
try {
  console.log('Raw commit data:', process.argv[2]);
  const commitData = JSON.parse(process.argv[2]);
  createCommitRecord(commitData);
} catch (error) {
  console.error('Error processing commit data:', error);
  process.exit(1);
}
