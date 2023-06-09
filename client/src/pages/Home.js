import styled from 'styled-components';

import Header from '../components/header/Header';
import Aside from '../components/leftAside/Aside';
import Main from '../components/main/Main';
import Footer from '../components/footer/Footer';
// import { StyledBody, AppContainer, StyledMain } from './styles/StyledApp';

// import RightSideBar from './components/RightSideBar';
// import Questions from './pages/Questions';
// import Tags from './pages/Tags';
// import Users from './pages/Users';
// import Companies from './pages/Companies';

const Container = styled.div`
  width: 100vw;
  /* height: 1000px; */
  background-color: white;
  display: flex;
  justify-content: center;
`;

const InnerContainer = styled.div`
  width: 1264px;
  /* height: 500px; */
  background-color: white;
  display: flex;
  /* flex-direction: column; */
  margin-top: 58px;
  position: relative;
`;

function Home() {
  return (
    <>
      <Header />
      <Container>
        <InnerContainer>
          <Aside />
          <Main />
        </InnerContainer>
      </Container>
      <Footer />
    </>
  );
}

export default Home;
