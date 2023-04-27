import { useEffect, useRef, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';

import styled from 'styled-components';
import axios from 'axios';
import Header from '../components/header/Header';
import Aside from '../components/leftAside/Aside';
import Footer from '../components/footer/Footer';
import useAxios from '../services/useAxios';
import useGetUserInfo from '../services/useGetUserInfo';
import {
  axiosCreate,
  axiosDelete,
  axiosPatch,
  axiosCreateAnswer,
  axiosDeleteAnswer,
  axiosDeleteComment,
} from '../services/api';

import { MarkDown } from '../components/feat/Input';
import StyledInputForm from '../styles/StyledInputForm';
import MarkdownViewer from '../components/feat/MarkDownViewer';

import DateWrap from '../components/question/DateWrap';
import VoteCell from '../components/question/VoteCell';
import AnswerSort from '../components/question/AnswerSort';
import PostWriter from '../components/question/PostWriter';
import AnswerWriter from '../components/question/AnswerWriter';

import { ReactComponent as Pencil } from '../assets/ic-pencil.svg';

const Container = styled.div`
  width: 100vw;
  background-color: white;
  display: flex;
  justify-content: center;
`;

const StyledQuestionContainer = styled.div`
  width: 850px;
  margin: 57px;
  h2 {
    font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI Adjusted', 'Segoe UI',
      'Liberation Sans', sans-serif;
    font-weight: normal;
    color: hsl(210, 8%, 25%);
  }
  h3 {
    font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI Adjusted', 'Segoe UI',
      'Liberation Sans', sans-serif;
    font-weight: normal;
    color: hsl(210, 8%, 25%);
  }
  padding: 24px;
  display: flex;
  flex-direction: column;
`;

const StyledQuestion = styled.div`
  margin-bottom: 20px;
`;
const QuestionHeader = styled.div`
  display: flex;
  flex-direction: row;
  justify-content: space-between;
`;

const AnswerLayout = styled.div`
  width: 100%;
  display: grid;
  grid-template-columns: max-content 1fr;
  font-size: 14px;
  border-bottom: 1px solid hsl(210, 8%, 95%);
  margin-bottom: 20px;
`;

const PostLayout = styled.div`
  width: 100%;
  display: flex;
  flex-direction: row;
  font-size: 14px;
`;

const PostBody = styled.div`
  p {
    margin-bottom: 1.1em;
    line-height: 1.5em;
  }
  code {
    font-size: 13px;
    font-family: ui-monospace, 'Cascadia Mono', 'Segoe UI Mono', 'Liberation Mono', Menlo,
      Monaco, Consolas, monospace;
  }
`;

const PostTags = styled.div``;

const PostCell = styled.div`
  width: 100%;
  button {
    background-color: transparent;
    border: none;
    color: gray;
  }
  .post-footer {
  }
  border-bottom: 1px solid hsl(210, 8%, 95%);
`;

const PostFooter = styled.div`
  width: 100%;
  .post-footer-wrap {
  }
`;

const PostFooterWrap = styled.div`
  display: flex;
  flex-direction: row;

  margin: 16px 0 16px 0;
  > * {
    flex-grow: 1;
    margin: 4px 0 4px 0;
  }
  .post-editor {
  }
  .user-info {
  }
`;

const PostEditor = styled.div`
  color: #2587d2;
  font-size: 12px;
  .editedtime {
    margin-left: 4px;
  }
`;

const ButtonWrap = styled.div``;

const PageButton = styled.button`
  background-color: ${props => (props.isActive ? '#f48225' : 'white')};
  color: ${props => (props.isActive ? 'white' : 'black')};
  border: ${props =>
    props.isActive ? '1px solid #f48225' : '1px solid hsl(210, 8%, 75%)'};

  width: 30px;
  height: 30px;
  border-radius: 5px;
  :hover {
    border: ${props =>
      props.isActive ? '1px solid #f48225' : '1px solid hsl(210, 8%, 75%)'};
    background-color: ${props => (props.isActive ? '#f48225' : 'hsl(210, 8%, 90%)')};
  }

  /* 선택된 버튼 스타일 */
  /* &.active {
    background-color: orange;
  } */
`;
const StyledAnswer = styled.div`
  display: flex;
  flex-direction: column;
  width: 100%;

  li {
    list-style: none;
  }
  .post-buttons {
    display: flex;
    flex-direction: row;
    gap: 4px;
  }
`;

const AnswersHeader = styled.div``;
const AnswersSubHeader = styled.div`
  display: flex;
  align-items: center;
  margin-bottom: 8px;
`;
const AnswersCount = styled.div`
  flex: 1 auto;
`;
const AnswerBody = styled.div``;

const EditingAnswer = styled.div``;
const EditingAnswerInput = styled.div`
  textarea {
    width: 100%;
    height: 200px;
  }
`;
const EditingAnswerButtons = styled.div``;

const ModalButtonWrap = styled.div``;

const EditErrorModal = styled.div`
  width: fit-content;
  padding: 12px;
  background-color: hsl(358, 62%, 47%);
  color: white;
  > button {
    margin-left: 12px;
    color: white;
  }
`;

const EditCommentErrorModal = styled.div`
  width: fit-content;
  margin-left: 20px;
  color: hsl(358, 62%, 47%);
  > button {
    margin-left: 12px;
    color: hsl(358, 62%, 47%);
    border: none;
    background-color: transparent;
  }
`;
const CancelButton = styled.button`
  background-color: transparent;
  border: none;
  color: #2587d2;
`;

const AnswersComments = styled.div``;

const CommentsContainer = styled.div`
  margin-bottom: 16px;
`;

const CmtListItem = styled.li`
  list-style: none;
  display: flex;
  flex-direction: row;
`;
const CmtAction = styled.div`
  padding: 6px;
  width: auto;
`;
const CmtScore = styled.div`
  margin-right: 10px;
  span {
    font-size: 12px;
    color: hsl(27, 90%, 55%);
    font-weight: 600;
  }
`;
const CmtText = styled.div`
  font-size: 12px;
  flex-grow: 1;
  padding: 6px;
  border-bottom: 1px solid hsl(210, 8%, 95%);
`;
const CmtBody = styled.div`
  word-wrap: break-word;
`;
const CmtCopy = styled.div`
  display: inline-flex;
  margin-right: 10px;
`;
const CmtUser = styled.a`
  color: #2587d2;
  display: inline-flex;
  margin-right: 10px;
`;

const CmtDate = styled.div`
  display: inline-flex;
  color: hsl(210, 8%, 60%);
  margin-right: 5px;
`;

const CmtEdit = styled.div`
  display: inline-flex;
  color: hsl(210, 8%, 60%);

  svg {
    fill: hsl(210, 8%, 100%);
  }
`;
const CommentsList = styled.ul``;

const CommentFormContainer = styled.div`
  display: flex;
  margin-top: 8px;
  justify-content: flex-end;
  flex-wrap: wrap;
`;

const CommentEditFormContainer = styled.div`
  display: flex;
  margin-top: 8px;
  justify-content: flex-start;
  flex-wrap: wrap;
`;

const CommentInputContainer = styled.div`
  textarea {
    width: 75%;
    height: 100px;
  }

  flex-grow: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
`;
const CommentEditContainer = styled.div`
  width: 75%;
  textarea {
    width: 100%;
    height: 100px;
  }

  display: flex;
  flex-direction: column;
  justify-content: space-between;
`;

const AddCommentInput = styled.div``;

const AddCommentMessage = styled.div`
  color: hsl(210, 8%, 60%);
  font-size: 12px;
`;
const CommentButtonContainer = styled.div`
  margin-left: 8px;
  margin-bottom: 16px;
  display: flex;
  justify-content: space-between;
  flex-direction: column;
`;

const AddButtonWrap = styled.div`
  margin-bottom: 10px;
`;
const HelpButtonWrap = styled.div`
  padding: 5px;
`;

const HelpButton = styled.div`
  background-color: transparent;
  border: none;
  color: #2587d2;
  font-size: 12px;
`;

const AddCommentButton = styled.button``;
const AddCommentForm = styled.form`
  width: 100%;
  margin-bottom: 20px;
`;
const EditCommentForm = styled.form`
  width: 100%;
  margin-bottom: 20px;
`;
const AddComment = styled.div``;
const CommentLinkContainer = styled.div`
  margin-bottom: 20px;
`;
const AddCommentLink = styled.a`
  color: #2587d2;
  cursor: pointer;
`;

const BlueButton = styled.button`
  background-color: #1e95ff;
  color: white;

  border-radius: 5px;
  padding: 12px 10px;
  width: fit-content;
  min-width: fit-content;
  /* boxshadow & border로 안쪽 입체감 주기 */
  box-shadow: inset 0px 1px 0px 0 #79c1ff;
  border: 1px solid #1e95ff;
  cursor: pointer;
`;

function Question() {
  // const devUrl = process.env.REACT_APP_DEV_URL;
  const { id } = useParams();
  const { questions, answers, pageInfos } = useAxios(`/api/questions/${id}`);
  // const [question, setQuestion] = useState(null);

  // // undefined 방지
  // useEffect(() => {
  //   setQuestion(questions);
  // }, [questions]);

  // token
  const [accessToken, setAccessToken] = useState(
    localStorage.getItem('accessToken') || '',
  );
  const [refreshToken, setRefreshToken] = useState(
    localStorage.getItem('refreshToken') || '',
  );

  // userInfo
  const { userInfo } = useGetUserInfo(`/api/members/info`);

  console.log(accessToken);

  // useEffect(() => {
  //   setQuestionData(questions);
  // }, [questions]);

  // page 별 answers 불러오기 위한 선언
  const [answersData, setAnswersData] = useState([]);
  // pageInfos가 Question에서 변경될 수 있기 때문에 useState로 관리
  const [pageInfosData, setPageInfosData] = useState(null);
  const navigate = useNavigate();
  const [currentPage, setCurrentPage] = useState(1);

  const [isQuestionModalOpen, setIsQuestionModalOpen] = useState(false);
  const [isAnswerModalOpen, setIsAnswerModalOpen] = useState(false);
  const [isCommentModalOpen, setIsCommentModalOpen] = useState(false);

  // answers가 바뀌면 answersData가 변할 수 있도록 useEffect 사용
  useEffect(() => {
    setAnswersData(answers);
    setPageInfosData(pageInfos);
  }, [answers, pageInfos]);

  // delete question
  const handleDelete = () => {
    if (userInfo.displayName !== questions.writtenBy) {
      setIsQuestionModalOpen(true);
      return;
    }
    axiosDelete(`/api/questions/${id}`);
  };

  // edit question 페이지로 이동
  const handleEdit = async () => {
    if (userInfo.displayName !== questions.writtenBy) {
      setIsQuestionModalOpen(true);
      return;
    }
    navigate(`/questions/${id}/edit`);
  };

  const editorAnswerRef = useRef();

  // add answer
  const handleAddAnswer = e => {
    e.preventDefault();
    const answerValue = editorAnswerRef.current?.getInstance().getHTML();
    // answer 하나만 보내면 어차피 갱신된 question을 보내주므로,
    // question PATCH 요청 X. answer을 POST 요청한다.
    const newAnswer = { content: answerValue };
    axiosCreateAnswer(
      `/api/questions/${id}/answers`,
      newAnswer,
      id,
      accessToken,
      refreshToken,
    );
  };

  // delete answer
  const handleDeleteAnswer = answer => {
    // if (현재 유저의 userid와 answer의 userid가 다르다) => return
    if (answer.writtenBy !== userInfo.displayName) {
      setIsAnswerModalOpen(true);
      return;
    }
    axiosDeleteAnswer(
      `/api/questions/${id}/answers/${answer.id}`,
      id,
      accessToken,
      refreshToken,
    );
  };

  // edit answer

  const [isEditingAnswer, setIsEditingAnswer] = useState(false);
  const [editingAnswerId, setEditingAnswerId] = useState('');
  const [preText, setPreText] = useState('');

  const editingAnswerRef = useRef();

  // answer 목록 중 수정할 answer의 에디터를 open
  const handleOpenAnswerEditor = answer => {
    if (answer.writtenBy !== userInfo.displayName) {
      setIsAnswerModalOpen(true);
      return;
    }
    // if (현재 유저의 userid와 answer의 userid가 다르다) => return
    setIsEditingAnswer(true);
    setEditingAnswerId(answer.id);
    // html 태그가 포함된 형태로 들어가지 않게 하기 위해 쏙 빼준다.
    // 근데 이렇게 하면 줄바꿈이 안 먹는다.
    // 어떻게 해볼까..
    const plainText = document.createElement('div');
    plainText.innerHTML = answer.content;
    setPreText(plainText.innerText);
  };

  // edit한 answer로 PATCH 요청
  const handleEditAnswer = answer => {
    const answerValue = editingAnswerRef.current?.getInstance().getHTML();
    const editedAnswer = {
      content: answerValue,
    };
    axiosPatch(
      `/api/questions/${id}/answers/${answer.id}`,
      editedAnswer,
      id,
      accessToken,
      refreshToken,
    );
    setIsEditingAnswer(false);
    setEditingAnswerId('');
    setPreText('');
  };

  // edit 취소
  const handleCancelEditAnswer = () => {
    setIsEditingAnswer(false);
    setEditingAnswerId('');
    setPreText('');
  };

  // comment

  const [commentInput, setCommentInput] = useState('');
  const [isCreatingComment, setIsCreatingComment] = useState(false);
  const [isEditingComment, setIsEditingComment] = useState(false);
  const [editingCommentId, setEditingCommentId] = useState('');
  const [cmtAnswerId, setCmtAnswerID] = useState('');

  const commentedAt = cmtedAt => {
    const time = new Date(Date.parse(cmtedAt)).toLocaleString('ko-KR', {
      timeZone: 'Asia/Seoul',
    });
    return time;
  };
  // 'add a comment'를 누르면 코멘트 작성창이 뜬다.
  const handleOpenCommentInput = answerId => {
    // if (현재 유저의 userid와 comment의 userid가 다르다) => return
    setIsCreatingComment(true);
    setCmtAnswerID(answerId);
  };

  // 입력 받은 comment로 state 갱신
  const handleComment = e => {
    setCommentInput(e.target.value);
  };

  // 입력 받은 comment로 POST 요청
  const handleAddComment = answerId => {
    const newComment = {
      text: commentInput,
    };
    // answerId만 있으면 상위 questionId까지 유추할 수 있음
    axiosCreateAnswer(
      `/api/answers/${answerId}/comments`,
      newComment,
      id,
      accessToken,
      refreshToken,
    );
    setIsCreatingComment(false);
    setCmtAnswerID('');
    setCommentInput('');
  };

  // comment 수정 창 열기
  const handleOpenCommentEditor = comment => {
    if (comment.writtenBy !== userInfo.displayName) {
      setIsCommentModalOpen(true);
      return;
    }
    setIsEditingComment(true);
    setEditingCommentId(comment.id);
    setCommentInput(comment.text);
  };

  // 수정한 comment로 PATCH 요청
  const handleEditComment = (answerId, comment) => {
    const editedComment = {
      text: commentInput,
    };
    axiosPatch(
      `/api/answers/${answerId}/comments/${comment.id}`,
      editedComment,
      id,
      accessToken,
      refreshToken,
    );
    setIsEditingComment(false);
    setEditingCommentId('');
    setCommentInput('');
  };

  // comment 수정 취소
  const handleCancelEditComment = () => {
    setIsEditingComment(false);
    setEditingCommentId('');
    setCommentInput('');
  };

  // delete comment
  const handleDeleteComment = (answerId, comment) => {
    if (comment.writtenBy !== userInfo.displayName) {
      setIsCommentModalOpen(true);
      return;
    }
    axiosDeleteComment(
      `/api/answers/${answerId}/comments/${comment.id}`,
      id,
      accessToken,
      refreshToken,
    );
  };

  // move to other answers page
  const handlePage = async page => {
    // 2라는 버튼을 누르면
    // axiosGet(`${devUrl}/questions/${id}?page=2`)

    navigate(`?page=${page}`);
    try {
      await axios(`/api/questions/${id}?page=${page}`, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
          'ngrok-skip-browser-warning': '69420',
        },
      })
        .then(response => {
          // url이 안바뀌니까 answer가 추가되어도 다시 데이터를 불러오지 않는다.
          // state 초기화
          setAnswersData(response.data.answers);
        })
        .catch(err => console.log(err.message));
      // 받아온 데이터를 처리하는 로직
    } catch (error) {
      // 에러 처리 로직
    }
    setCurrentPage(page);
  };

  // page 버튼을 모을 배열 생성
  const pageButtons = [];

  // totalPages의 정보를 토대로 page 개수 지정해서 생성
  if (pageInfosData) {
    for (let i = 1; i <= pageInfosData.totalPages; i += 1) {
      const isActive = currentPage === i;

      pageButtons.push(
        <PageButton key={i} isActive={isActive} onClick={() => handlePage(i)}>
          {i}
        </PageButton>,
      );
    }
  }

  return (
    <>
      <Container>
        <Header />
        <Aside />
        <StyledQuestionContainer>
          <StyledQuestion>
            <QuestionHeader>
              <h2>{questions.title}</h2>
              <BlueButton
                onClick={() => {
                  navigate('/questions/ask');
                }}
              >
                Ask Question
              </BlueButton>
            </QuestionHeader>
            <DateWrap />
            <PostLayout>
              <VoteCell />
              <PostCell>
                <PostBody>
                  <MarkdownViewer content={questions.body} />
                  <PostTags />
                  <PostFooter>
                    <PostFooterWrap>
                      <ModalButtonWrap>
                        <ButtonWrap>
                          <button type="button">share</button>

                          <button type="button" onClick={handleEdit}>
                            edit
                          </button>

                          <button type="button" onClick={handleDelete}>
                            delete
                          </button>
                          <button type="button">follow</button>
                        </ButtonWrap>
                        {isQuestionModalOpen && (
                          <EditErrorModal>
                            <span>Account is suspended.</span>
                            <button
                              onClick={() => {
                                setIsQuestionModalOpen(false);
                              }}
                            >
                              x
                            </button>
                          </EditErrorModal>
                        )}
                      </ModalButtonWrap>
                      <PostEditor>
                        <span>edited</span>
                        <span className="editedtime">Dec 23, 2021 at 20:30</span>
                      </PostEditor>
                      <PostWriter question={questions} />
                    </PostFooterWrap>
                  </PostFooter>
                </PostBody>
              </PostCell>
            </PostLayout>
          </StyledQuestion>
          {answersData.length ? (
            <StyledAnswer>
              <AnswersHeader>
                <AnswersSubHeader>
                  <AnswersCount>
                    {pageInfosData ? (
                      <h3>{!pageInfosData ? 0 : pageInfosData.totalElements} Answers</h3>
                    ) : null}
                  </AnswersCount>
                  <AnswerSort />
                </AnswersSubHeader>
              </AnswersHeader>
              <div className="buttonContainer">{pageButtons}</div>
              {!answersData
                ? null
                : answersData.map(answer => {
                    return (
                      <ul>
                        <li key={answer.id}>
                          <AnswerLayout>
                            {isEditingAnswer && editingAnswerId === answer.id ? (
                              <EditingAnswer>
                                <EditingAnswerInput>
                                  <StyledInputForm
                                    onSubmit={() => {
                                      handleEditAnswer(answer);
                                    }}
                                  >
                                    <h3>Your Answer</h3>
                                    <MarkDown
                                      editorRef={editingAnswerRef}
                                      preText={preText}
                                    />
                                    <div className="form-submit">
                                      <BlueButton type="submit">
                                        Edit Your Answer
                                      </BlueButton>
                                      <CancelButton onClick={handleCancelEditAnswer}>
                                        cancel
                                      </CancelButton>
                                    </div>
                                  </StyledInputForm>
                                </EditingAnswerInput>
                              </EditingAnswer>
                            ) : (
                              <>
                                <VoteCell />
                                <PostCell>
                                  <AnswerBody>
                                    <MarkdownViewer content={answer.content} />
                                  </AnswerBody>
                                  <PostFooter>
                                    <PostFooterWrap>
                                      <ButtonWrap>
                                        <button type="button">share</button>
                                        <button
                                          type="button"
                                          onClick={() => handleOpenAnswerEditor(answer)}
                                        >
                                          edit
                                        </button>
                                        <button
                                          type="button"
                                          onClick={() => handleDeleteAnswer(answer)}
                                        >
                                          delete
                                        </button>
                                        <button type="button">flag</button>
                                      </ButtonWrap>
                                      {isAnswerModalOpen && (
                                        <EditErrorModal>
                                          <span>Account is suspended.</span>
                                          <button
                                            onClick={() => {
                                              setIsAnswerModalOpen(false);
                                            }}
                                          >
                                            x
                                          </button>
                                        </EditErrorModal>
                                      )}
                                      <PostEditor>
                                        <span>edited</span>
                                        <span className="editedtime">
                                          Dec 23, 2021 at 20:30
                                        </span>
                                      </PostEditor>
                                      <AnswerWriter answer={answer} />
                                    </PostFooterWrap>
                                  </PostFooter>
                                </PostCell>
                              </>
                            )}
                            <div className="dummy" />
                            <AnswersComments>
                              <CommentsContainer>
                                <CommentsList>
                                  {answer.comments.map(comment => {
                                    return (
                                      <CmtListItem key={comment.id}>
                                        {!(
                                          isEditingComment &&
                                          editingCommentId === comment.id
                                        ) ? (
                                          <>
                                            <CmtAction>
                                              <CmtScore>
                                                <span>124</span>
                                              </CmtScore>
                                            </CmtAction>
                                            <CmtText>
                                              <CmtBody>
                                                <CmtCopy>{comment.text}</CmtCopy>
                                                <CmtUser>{comment.writtenBy}</CmtUser>
                                                <CmtDate>
                                                  {commentedAt(comment.createdAt)}
                                                </CmtDate>
                                                <CmtEdit>
                                                  <Pencil
                                                    onClick={() => {
                                                      handleOpenCommentEditor(comment);
                                                    }}
                                                  />
                                                  <CancelButton
                                                    onClick={() => {
                                                      handleDeleteComment(
                                                        answer.id,
                                                        comment,
                                                      );
                                                    }}
                                                  >
                                                    delete
                                                  </CancelButton>
                                                  {isCommentModalOpen && (
                                                    <EditCommentErrorModal>
                                                      <span>Account is suspended.</span>
                                                      <button
                                                        onClick={() => {
                                                          setIsCommentModalOpen(false);
                                                        }}
                                                      >
                                                        x
                                                      </button>
                                                    </EditCommentErrorModal>
                                                  )}
                                                </CmtEdit>
                                              </CmtBody>
                                            </CmtText>
                                          </>
                                        ) : (
                                          <EditCommentForm
                                            onSubmit={() =>
                                              handleEditComment(answer.id, comment)
                                            }
                                          >
                                            <CommentEditFormContainer>
                                              <CommentEditContainer>
                                                <AddCommentInput>
                                                  <textarea
                                                    value={commentInput}
                                                    onChange={handleComment}
                                                  />
                                                </AddCommentInput>
                                                <AddCommentMessage>
                                                  Enter at least 15 characters
                                                </AddCommentMessage>
                                              </CommentEditContainer>
                                              <AddComment>
                                                <CommentButtonContainer>
                                                  <AddButtonWrap>
                                                    <BlueButton type="submit">
                                                      Edit comment
                                                    </BlueButton>
                                                  </AddButtonWrap>
                                                  <HelpButtonWrap>
                                                    <HelpButton
                                                      onClick={handleCancelEditComment}
                                                    >
                                                      Cancel
                                                    </HelpButton>
                                                  </HelpButtonWrap>
                                                </CommentButtonContainer>
                                              </AddComment>
                                            </CommentEditFormContainer>
                                          </EditCommentForm>
                                        )}
                                      </CmtListItem>
                                    );
                                  })}
                                </CommentsList>
                              </CommentsContainer>
                              {isCreatingComment && answer.id === cmtAnswerId ? (
                                <AddCommentForm
                                  onSubmit={() => handleAddComment(answer.id)}
                                >
                                  <CommentFormContainer>
                                    <CommentInputContainer>
                                      <AddCommentInput>
                                        <textarea onChange={handleComment} />
                                      </AddCommentInput>
                                      <AddCommentMessage>
                                        Enter at least 15 characters
                                      </AddCommentMessage>
                                    </CommentInputContainer>
                                    <AddComment>
                                      <CommentButtonContainer>
                                        <AddButtonWrap>
                                          <BlueButton type="submit">
                                            Add comment
                                          </BlueButton>
                                        </AddButtonWrap>
                                        <HelpButtonWrap>
                                          <HelpButton>Help</HelpButton>
                                        </HelpButtonWrap>
                                      </CommentButtonContainer>
                                    </AddComment>
                                  </CommentFormContainer>
                                </AddCommentForm>
                              ) : (
                                <CommentLinkContainer>
                                  <AddCommentLink
                                    onClick={() => {
                                      handleOpenCommentInput(answer.id);
                                    }}
                                  >
                                    Add a comment
                                  </AddCommentLink>
                                </CommentLinkContainer>
                              )}
                            </AnswersComments>
                          </AnswerLayout>
                        </li>
                      </ul>
                    );
                  })}
            </StyledAnswer>
          ) : null}

          <StyledInputForm onSubmit={handleAddAnswer}>
            <h3>Your Answer</h3>
            <MarkDown editorRef={editorAnswerRef} />
            <div className="form-submit">
              <BlueButton type="submit">Post Your Answer</BlueButton>
            </div>
          </StyledInputForm>
        </StyledQuestionContainer>
      </Container>
      <Footer />
    </>
  );
}

export default Question;
